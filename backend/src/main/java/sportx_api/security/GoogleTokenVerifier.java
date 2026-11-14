package sportx_api.security;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Verifica el ID token (JWT) que entrega Google Identity Services al frontend:
 * firma RS256 con las claves públicas de Google, emisor, audiencia (nuestro
 * client ID), vigencia y correo verificado.
 *
 * No se registra como bean JwtDecoder para no interferir con el decodificador
 * de los tokens propios de SportX.
 */
@Component
public class GoogleTokenVerifier {

    private static final Set<String> EMISORES = Set.of(
        "https://accounts.google.com", "accounts.google.com");

    private final String clientId;
    private final NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(
            @Value("${sportx.google.client-id:}") String clientId,
            @Value("${sportx.google.jwk-set-uri:https://www.googleapis.com/oauth2/v3/certs}")
            String jwkSetUri) {

        this.clientId = clientId == null ? "" : clientId.trim();
        this.decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> emisor = jwt -> EMISORES.contains(jwt.getClaimAsString("iss"))
            ? OAuth2TokenValidatorResult.success()
            : fallo("Emisor del token de Google no válido");

        OAuth2TokenValidator<Jwt> audiencia = jwt -> {
            List<String> aud = jwt.getAudience();
            return aud != null && aud.contains(this.clientId)
                ? OAuth2TokenValidatorResult.success()
                : fallo("El token de Google no es para esta aplicación");
        };

        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), emisor, audiencia));
    }

    public boolean configurado() {
        return !clientId.isBlank();
    }

    /** Devuelve los datos verificados de la cuenta Google o lanza 401. */
    public GoogleUser verificar(String credential) {

        if (!configurado()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El inicio de sesión con Google no está configurado en el servidor");
        }

        if (credential == null || credential.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Falta la credencial de Google");
        }

        Jwt jwt;
        try {
            jwt = decoder.decode(credential);
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "No se pudo validar tu cuenta de Google. Intenta nuevamente.");
        }

        Object verificado = jwt.getClaims().get("email_verified");
        boolean emailVerificado = Boolean.TRUE.equals(verificado)
            || "true".equalsIgnoreCase(String.valueOf(verificado));

        String email = jwt.getClaimAsString("email");

        if (email == null || !emailVerificado) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Tu cuenta de Google no tiene un correo verificado");
        }

        return new GoogleUser(
            jwt.getSubject(),
            email.trim().toLowerCase(),
            jwt.getClaimAsString("given_name"),
            jwt.getClaimAsString("family_name"));
    }

    private static OAuth2TokenValidatorResult fallo(String mensaje) {
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", mensaje, null));
    }

    public record GoogleUser(String sub, String email, String nombre, String apellidos) {
    }
}
