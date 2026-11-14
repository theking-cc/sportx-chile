package sportx_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
    Long id,
    String numero,
    Long idUsuario,
    String destinatario,
    String email,
    LocalDateTime fecha,
    String estado,
    BigDecimal subtotal,
    BigDecimal descuento,
    BigDecimal total,
    String codigoCupon,
    String direccion,
    String region,
    String comuna,
    List<OrdenItemResponse> productos,
    PagoInfo pago
) {
    /** Estado del pago Webpay asociado (null si la orden no pasó por Webpay). */
    public record PagoInfo(
        String estado,
        String codigoAutorizacion,
        String tipoPago,
        Integer cuotas,
        String ultimosDigitos) {
    }
}
