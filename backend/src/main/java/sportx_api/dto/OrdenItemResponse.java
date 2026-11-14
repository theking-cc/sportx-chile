package sportx_api.dto;

import java.math.BigDecimal;

public record OrdenItemResponse(
    Long idProducto,
    String codigo,
    String nombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {
}
