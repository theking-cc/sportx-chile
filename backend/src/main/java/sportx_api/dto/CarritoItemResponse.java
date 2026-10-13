package sportx_api.dto;

import java.math.BigDecimal;

public record CarritoItemResponse(
    Long idDetalle,
    Long idProducto,
    String codigo,
    String nombre,
    BigDecimal precioUnitario,
    Integer cantidad,
    BigDecimal subtotal
) {}
