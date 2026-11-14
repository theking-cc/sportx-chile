package sportx_api.pago;

import java.math.BigDecimal;

/** Pasarela de pago con redirección (Webpay Plus o su simulador). */
public interface PasarelaPago {

    /** Crea la transacción y devuelve a dónde enviar al comprador. */
    Inicio crear(String ordenCompra, String sesion, BigDecimal monto, String urlRetorno);

    /** Confirma la transacción con el token devuelto por la pasarela. */
    Resultado confirmar(String token);

    record Inicio(String url, String token) {
    }

    record Resultado(
        boolean aprobado,
        Integer codigoRespuesta,
        String estado,
        String ordenCompra,
        BigDecimal monto,
        String codigoAutorizacion,
        String tipoPago,
        Integer cuotas,
        String ultimosDigitos) {
    }
}
