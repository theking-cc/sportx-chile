package sportx_api.dto;

import java.math.BigDecimal;

public record ProductoResponse(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Integer stock,
    Integer stockCritico,
    String imagenUrl,
    Long categoriaId,
    String categoria
) {
}