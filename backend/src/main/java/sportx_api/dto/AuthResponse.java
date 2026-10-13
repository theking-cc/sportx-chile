package sportx_api.dto;

public record AuthResponse(

    Long id,
    String nombre,
    String apellidos,
    String email,
    String rol,
    String token

) {
}
