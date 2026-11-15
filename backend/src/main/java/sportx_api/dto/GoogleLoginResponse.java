package sportx_api.dto;

/**
 * Resultado del inicio con Google.
 * - registroPendiente = false: la sesión viene en "sesion".
 * - registroPendiente = true: la cuenta no existe; el frontend debe pedir
 *   RUT, comuna y dirección y llamar a /api/v1/auth/google/registro.
 */
public record GoogleLoginResponse(
    boolean registroPendiente,
    String email,
    String nombre,
    String apellidos,
    AuthResponse sesion
) {
}
