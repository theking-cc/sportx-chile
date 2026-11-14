package sportx_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajeRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 100)
    String email,

    @Size(max = 150)
    String asunto,

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje admite máximo 1000 caracteres")
    String mensaje

) {
}
