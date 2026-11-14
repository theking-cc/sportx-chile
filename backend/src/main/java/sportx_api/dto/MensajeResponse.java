package sportx_api.dto;

import java.time.LocalDateTime;

public record MensajeResponse(
    Long id,
    Long idUsuario,
    String nombre,
    String email,
    String asunto,
    String mensaje,
    String estado,
    LocalDateTime fecha
) {
}
