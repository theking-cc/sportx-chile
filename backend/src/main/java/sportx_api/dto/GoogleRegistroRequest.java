package sportx_api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

// Datos que Google no entrega y la base de datos exige para crear la cuenta.
public record GoogleRegistroRequest(

    @NotBlank(message = "Falta la credencial de Google")
    String credential,

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12)
    String rut,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    String apellidos,

    @NotNull(message = "La comuna es obligatoria")
    Long idComuna,

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 300)
    String direccion,

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    LocalDate fechaNacimiento

) {
}
