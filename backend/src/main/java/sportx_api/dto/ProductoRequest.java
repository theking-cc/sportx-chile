
package sportx_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50)
    String codigo,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    String nombre,

    String descripcion,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    BigDecimal precio,

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero
    Integer stock,

    @NotNull(message = "El stock crítico es obligatorio")
    @PositiveOrZero
    Integer stockCritico,

    @Size(max = 500)
    String imagenUrl,

    @NotNull(message = "La categoría es obligatoria")
    Long categoriaId
) {
}
