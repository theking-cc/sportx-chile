
package sportx_api.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import sportx_api.model.Usuario;
import sportx_api.repository.UsuarioRepository;

@Component
public class UsuarioJwtValidator
        implements OAuth2TokenValidator<Jwt> {

    private final UsuarioRepository usuarioRepository;

    public UsuarioJwtValidator(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        System.out.println("=== VALIDANDO JWT ===");

        try {

            String subject = jwt.getSubject();

            System.out.println("JWT subject: " + subject);

            if (subject == null || subject.isBlank()) {
                return invalido(
                    "El token no contiene un usuario"
                );
            }

            Long usuarioId;

            try {
                usuarioId = Long.valueOf(subject);
            } catch (NumberFormatException ex) {
                return invalido(
                    "El identificador del usuario no es válido"
                );
            }

            // BUSCAR USUARIO CON SU ROL CARGADO
            Usuario usuario = usuarioRepository
                .findByIdConRol(usuarioId)
                .orElse(null);

            if (usuario == null) {
                return invalido(
                    "El usuario no existe"
                );
            }

            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                return invalido(
                    "El usuario está desactivado"
                );
            }

            if (usuario.getRol() == null) {
                return invalido(
                    "El usuario no tiene un rol asignado"
                );
            }

            String rolToken =
                jwt.getClaimAsString("rol");

            String rolActual =
                usuario.getRol().getNombre();

            System.out.println(
                "Rol del JWT: " + rolToken
            );

            System.out.println(
                "Rol de MariaDB: " + rolActual
            );

            if (rolToken == null
                    || !rolToken.equals(rolActual)) {

                return invalido(
                    "El rol del token no coincide"
                );
            }

            System.out.println(
                "JWT VALIDADO CORRECTAMENTE"
            );

            System.out.println("=========================");

            return OAuth2TokenValidatorResult.success();

        } catch (RuntimeException ex) {

            System.err.println(
                "Error al validar JWT: "
                    + ex.getClass().getSimpleName()
                    + " - "
                    + ex.getMessage()
            );

            return invalido(
                "No fue posible validar el usuario"
            );
        }
    }

    private OAuth2TokenValidatorResult invalido(
            String mensaje) {

        System.err.println(
            "JWT RECHAZADO: " + mensaje
        );

        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error(
                "invalid_token",
                mensaje,
                null
            )
        );
    }
}
