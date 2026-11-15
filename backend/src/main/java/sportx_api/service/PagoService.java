package sportx_api.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.CrearOrdenRequest;
import sportx_api.dto.IniciarPagoResponse;
import sportx_api.model.Orden;
import sportx_api.model.Pago;
import sportx_api.pago.PasarelaPago;
import sportx_api.repository.OrdenRepository;
import sportx_api.repository.PagoRepository;

/**
 * Flujo Webpay Plus:
 * 1) iniciar: crea la orden (reserva stock), registra el pago INICIADO y crea la
 *    transacción en Transbank. El carrito NO se vacía todavía.
 * 2) retorno: Transbank devuelve al comprador con token_ws (pagó o fue rechazado),
 *    TBK_TOKEN (anuló en el formulario) o solo TBK_ORDEN_COMPRA (tiempo agotado).
 *    Aprobado → vacía el carrito. Rechazado/anulado/expirado → cancela la orden
 *    y repone el stock. Cada pago se procesa una sola vez.
 */
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final OrdenService ordenService;
    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final PasarelaPago pasarela;
    private final String frontendUrl;

    public PagoService(
            OrdenService ordenService,
            OrdenRepository ordenRepository,
            PagoRepository pagoRepository,
            PasarelaPago pasarela,
            @Value("${sportx.frontend-url}") String frontendUrl) {

        this.ordenService = ordenService;
        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
        this.pasarela = pasarela;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    // =========================================
    // 1. INICIAR PAGO
    // =========================================
    @Transactional
    public IniciarPagoResponse iniciar(Long usuarioId, CrearOrdenRequest datos) {

        Orden orden = ordenService.reservar(usuarioId, datos);

        // Webpay no cobra montos de $0: la orden queda registrada directamente.
        if (orden.getMontoTotal().compareTo(BigDecimal.ZERO) <= 0) {
            ordenService.vaciarCarrito(usuarioId);
            return new IniciarPagoResponse(true, orden.getId(), null, null);
        }

        Pago pago = new Pago();
        pago.setIdOrden(orden.getId());
        pago.setOrdenCompra("SX" + orden.getId()); // máx. 26 caracteres
        pago.setMonto(orden.getMontoTotal());
        pago.setEstado("INICIADO");

        // Si Transbank falla, la excepción revierte la orden y la reserva de stock.
        PasarelaPago.Inicio inicio = pasarela.crear(
            pago.getOrdenCompra(),
            "U" + usuarioId,
            orden.getMontoTotal(),
            frontendUrl + "/api/v1/pagos/webpay/retorno");

        pago.setToken(inicio.token());
        pagoRepository.save(pago);

        return new IniciarPagoResponse(false, orden.getId(), inicio.url(), inicio.token());
    }

    // =========================================
    // 2. RETORNO DESDE TRANSBANK → URL del frontend con el resultado
    // =========================================
    @Transactional
    public String retorno(String tokenWs, String tbkToken, String tbkOrdenCompra) {

        boolean anulado = tbkToken != null && !tbkToken.isBlank();
        boolean conToken = tokenWs != null && !tokenWs.isBlank();

        // a) El comprador anuló en el formulario de Webpay (o hubo un error en él)
        if (anulado) {
            return pagoRepository.findByToken(tbkToken)
                .map(p -> cerrarSinPago(p, "ANULADO", "anulado"))
                .orElse(resultado("error", null));
        }

        // b) Pago aprobado o rechazado: se confirma con Transbank
        if (conToken) {
            Pago pago = pagoRepository.findByToken(tokenWs).orElse(null);
            if (pago == null) {
                return resultado("error", null);
            }
            if (!"INICIADO".equals(pago.getEstado())) {
                // Retorno repetido (recarga del navegador): no se vuelve a confirmar.
                return resultado(estadoFrontend(pago.getEstado()), pago.getIdOrden());
            }
            PasarelaPago.Resultado r;
            try {
                r = pasarela.confirmar(tokenWs);
            } catch (ResponseStatusException ex) {
                log.warn("Fallo al confirmar el pago {}: {}", pago.getOrdenCompra(), ex.getReason());
                return cerrarSinPago(pago, "ERROR", "error");
            }

            pago.setCodigoRespuesta(r.codigoRespuesta());
            pago.setCodigoAutorizacion(r.codigoAutorizacion());
            pago.setTipoPago(r.tipoPago());
            pago.setCuotas(r.cuotas());
            pago.setUltimosDigitos(r.ultimosDigitos());

            boolean coincide = r.monto() != null
                && r.monto().compareTo(pago.getMonto()) == 0
                && pago.getOrdenCompra().equals(r.ordenCompra());

            if (r.aprobado() && coincide) {
                pago.setEstado("APROBADO");
                pagoRepository.save(pago);
                ordenRepository.findById(pago.getIdOrden())
                    .ifPresent(o -> ordenService.vaciarCarrito(o.getIdUsuario()));
                return resultado("aprobado", pago.getIdOrden());
            }

            if (r.aprobado() && !coincide) {
                log.error("Pago {} aprobado con monto u orden distintos: {}", pago.getOrdenCompra(), r);
            }
            return cerrarSinPago(pago, "RECHAZADO", "rechazado");
        }

        // c) Tiempo agotado en el formulario: solo llega la orden de compra
        if (tbkOrdenCompra != null && !tbkOrdenCompra.isBlank()) {
            return pagoRepository.findByOrdenCompra(tbkOrdenCompra)
                .map(p -> cerrarSinPago(p, "ANULADO", "expirado"))
                .orElse(resultado("error", null));
        }

        return resultado("error", null);
    }

    private String cerrarSinPago(Pago pago, String estadoPago, String estadoFrontend) {
        if (!"INICIADO".equals(pago.getEstado())) {
            // Ya procesado antes (retorno repetido): se informa el estado guardado.
            return resultado(estadoFrontend(pago.getEstado()), pago.getIdOrden());
        }
        pago.setEstado(estadoPago);
        pagoRepository.save(pago);
        ordenService.cancelarPorPagoFallido(pago.getIdOrden());
        return resultado(estadoFrontend, pago.getIdOrden());
    }

    private static String estadoFrontend(String estadoPago) {
        return switch (estadoPago) {
            case "APROBADO" -> "aprobado";
            case "RECHAZADO" -> "rechazado";
            case "ANULADO" -> "anulado";
            default -> "error";
        };
    }

    private String resultado(String estado, Long idOrden) {
        return frontendUrl + "/#/pago/resultado?estado=" + estado
            + (idOrden == null ? "" : "&orden=" + idOrden);
    }
}
