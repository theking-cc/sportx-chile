package sportx_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(
    Long idCarrito,
    Long idUsuario,
    List<CarritoItemResponse> productos,
    BigDecimal total
) {}
