package sportx_api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

    @NotBlank(message = "Falta la credencial de Google")
    String credential

) {
}
