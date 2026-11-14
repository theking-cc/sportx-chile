package sportx_api.dto;

import java.time.LocalDate;

public record UsuarioResponse(
    Long id,
    String rut,
    String nombre,
    String apellidos,
    String email,
    String rol,
    Long idComuna,
    String comuna,
    String region,
    String direccion,
    LocalDate fechaNacimiento
) {
}
