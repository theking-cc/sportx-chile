package sportx_api.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import sportx_api.model.Usuario;
import sportx_api.repository.UsuarioRepository;

@Component
public class UsuarioJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final UsuarioRepository usuarioRepository;

    public UsuarioJwtValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        try {
            Long usuarioId = Long.valueOf(jwt.getSubject());

            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElse(null);

            if (usuario == null
                    || !Boolean.TRUE.equals(usuario.getActivo())) {
                return invalido("Usuario inexistente o desactivado");
            }

            String rolToken = jwt.getClaimAsString("rol");
            String rolActual = usuario.getRol().getNombre();

            if (rolToken == null || !rolToken.equals(rolActual)) {
                return invalido("El rol del usuario ha cambiado");
            }

            return OAuth2TokenValidatorResult.success();

        } catch (RuntimeException ex) {
            return invalido("No fue posible validar el usuario");
        }
    }

    private OAuth2TokenValidatorResult invalido(String mensaje) {

        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error(
                "invalid_token",
                mensaje,
                null
            )
        );
    }
}
