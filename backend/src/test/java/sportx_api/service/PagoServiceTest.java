package sportx_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.CrearOrdenRequest;
import sportx_api.dto.IniciarPagoResponse;
import sportx_api.model.Orden;
import sportx_api.model.Pago;
import sportx_api.pago.PasarelaPago;
import sportx_api.repository.OrdenRepository;
import sportx_api.repository.PagoRepository;

/** Lógica del pago con Webpay Plus (pasarela simulada con Mockito, sin base de datos). */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    private static final String FRONT = "http://127.0.0.1:5173";

    @Mock OrdenService ordenService;
    @Mock OrdenRepository ordenRepository;
    @Mock PagoRepository pagoRepository;
    @Mock PasarelaPago pasarela;

    PagoService service;

    @BeforeEach
    void crearServicio() {
        service = new PagoService(ordenService, ordenRepository, pagoRepository, pasarela, FRONT);
    }

    private Pago pagoIniciado() {
        Pago p = new Pago();
        p.setIdOrden(10L);
        p.setOrdenCompra("SX10");
        p.setToken("tok");
        p.setMonto(new BigDecimal("79990"));
        p.setEstado("INICIADO");
        return p;
    }

    private PasarelaPago.Resultado resultado(boolean aprobado, String monto, String orden) {
        return new PasarelaPago.Resultado(aprobado, aprobado ? 0 : -1,
            aprobado ? "AUTHORIZED" : "FAILED", orden, new BigDecimal(monto),
            aprobado ? "1213" : null, "VD", 0, "6623");
    }

    @Test
    @DisplayName("Pago aprobado con monto y orden correctos: APROBADO y vacía el carrito")
    void aprobado() {
        Pago pago = pagoIniciado();
        Orden orden = new Orden();
        orden.setIdUsuario(5L);
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));
        when(pasarela.confirmar("tok")).thenReturn(resultado(true, "79990", "SX10"));
        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

        String url = service.retorno("tok", null, null);

        assertThat(pago.getEstado()).isEqualTo("APROBADO");
        assertThat(url).isEqualTo(FRONT + "/#/pago/resultado?estado=aprobado&orden=10");
        verify(ordenService).vaciarCarrito(5L);
        verify(ordenService, never()).cancelarPorPagoFallido(any());
    }

    @Test
    @DisplayName("Aprobado por Transbank pero con monto distinto: se rechaza y se cancela la orden")
    void montoAlterado() {
        Pago pago = pagoIniciado();
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));
        when(pasarela.confirmar("tok")).thenReturn(resultado(true, "100", "SX10"));

        String url = service.retorno("tok", null, null);

        assertThat(pago.getEstado()).isEqualTo("RECHAZADO");
        assertThat(url).contains("estado=rechazado");
        verify(ordenService).cancelarPorPagoFallido(10L);
    }

    @Test
    @DisplayName("Tarjeta rechazada: RECHAZADO, orden cancelada y stock repuesto")
    void rechazado() {
        Pago pago = pagoIniciado();
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));
        when(pasarela.confirmar("tok")).thenReturn(resultado(false, "79990", "SX10"));

        assertThat(service.retorno("tok", null, null)).contains("estado=rechazado");
        assertThat(pago.getEstado()).isEqualTo("RECHAZADO");
        verify(ordenService).cancelarPorPagoFallido(10L);
    }

    @Test
    @DisplayName("Comprador anula en Webpay (TBK_TOKEN): ANULADO sin confirmar con Transbank")
    void anulado() {
        Pago pago = pagoIniciado();
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));

        assertThat(service.retorno(null, "tok", "SX10")).contains("estado=anulado");
        assertThat(pago.getEstado()).isEqualTo("ANULADO");
        verify(pasarela, never()).confirmar(anyString());
        verify(ordenService).cancelarPorPagoFallido(10L);
    }

    @Test
    @DisplayName("Tiempo agotado (solo TBK_ORDEN_COMPRA): ANULADO y resultado expirado")
    void tiempoAgotado() {
        Pago pago = pagoIniciado();
        when(pagoRepository.findByOrdenCompra("SX10")).thenReturn(Optional.of(pago));

        assertThat(service.retorno(null, null, "SX10")).contains("estado=expirado");
        assertThat(pago.getEstado()).isEqualTo("ANULADO");
    }

    @Test
    @DisplayName("Retorno repetido (recarga): no vuelve a confirmar ni a cobrar")
    void retornoRepetido() {
        Pago pago = pagoIniciado();
        pago.setEstado("APROBADO");
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));

        assertThat(service.retorno("tok", null, null)).contains("estado=aprobado");
        verify(pasarela, never()).confirmar(anyString());
    }

    @Test
    @DisplayName("Error al confirmar con Transbank: ERROR y la orden se cancela")
    void errorPasarela() {
        Pago pago = pagoIniciado();
        when(pagoRepository.findByToken("tok")).thenReturn(Optional.of(pago));
        when(pasarela.confirmar("tok"))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Transbank no responde"));

        assertThat(service.retorno("tok", null, null)).contains("estado=error");
        assertThat(pago.getEstado()).isEqualTo("ERROR");
        verify(ordenService).cancelarPorPagoFallido(10L);
    }

    @Test
    @DisplayName("Token desconocido: resultado de error sin tocar ninguna orden")
    void tokenDesconocido() {
        when(pagoRepository.findByToken("x")).thenReturn(Optional.empty());

        assertThat(service.retorno("x", null, null)).endsWith("estado=error");
        verify(ordenService, never()).cancelarPorPagoFallido(any());
    }

    @Test
    @DisplayName("Orden de $0: se registra sin pasar por Webpay")
    void ordenGratis() {
        Orden orden = new Orden();
        ReflectionTestUtils.setField(orden, "id", 7L);
        orden.setMontoTotal(BigDecimal.ZERO);
        CrearOrdenRequest datos = new CrearOrdenRequest("Calle 1", 1L, null);
        when(ordenService.reservar(5L, datos)).thenReturn(orden);

        IniciarPagoResponse r = service.iniciar(5L, datos);

        assertThat(r.gratis()).isTrue();
        verify(pasarela, never()).crear(anyString(), anyString(), any(), anyString());
        verify(ordenService).vaciarCarrito(5L);
    }

    @Test
    @DisplayName("Orden pagada: crea la transacción con orden SX<id> y URL de retorno versionada")
    void iniciarConWebpay() {
        Orden orden = new Orden();
        ReflectionTestUtils.setField(orden, "id", 7L);
        orden.setMontoTotal(new BigDecimal("24990"));
        CrearOrdenRequest datos = new CrearOrdenRequest("Calle 1", 1L, null);
        when(ordenService.reservar(5L, datos)).thenReturn(orden);
        when(pasarela.crear("SX7", "U5", new BigDecimal("24990"),
                FRONT + "/api/v1/pagos/webpay/retorno"))
            .thenReturn(new PasarelaPago.Inicio("https://webpay/form", "tok7"));

        IniciarPagoResponse r = service.iniciar(5L, datos);

        assertThat(r.gratis()).isFalse();
        assertThat(r.url()).isEqualTo("https://webpay/form");
        assertThat(r.token()).isEqualTo("tok7");
        verify(ordenService, never()).vaciarCarrito(any());
    }
}
