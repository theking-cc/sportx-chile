package sportx_api.dto;

/**
 * Respuesta al iniciar un pago.
 * - gratis = true: el total era $0, la orden quedó registrada sin pasar por Webpay.
 * - gratis = false: el frontend debe enviar un formulario POST a "url" con el campo token_ws = token.
 */
public record IniciarPagoResponse(
    boolean gratis,
    Long idOrden,
    String url,
    String token
) {
}
