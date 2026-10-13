
package sportx_api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12)
    String rut,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    String apellidos,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password,

    @NotNull(message = "La comuna es obligatoria")
    Long idComuna,

    @NotBlank(message = "La dirección es obligatoria")
    String direccion,

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    LocalDate fechaNacimiento

) {
}
