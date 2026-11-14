package sportx_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearOrdenRequest(

    @NotBlank(message = "La dirección de despacho es obligatoria")
    @Size(max = 300, message = "La dirección admite máximo 300 caracteres")
    String direccion,

    @NotNull(message = "La comuna de despacho es obligatoria")
    Long idComuna,

    @Size(max = 50)
    String codigoCupon

) {
}
