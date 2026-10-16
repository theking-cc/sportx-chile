
package sportx_api.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import sportx_api.security.UsuarioJwtValidator;

@Configuration
public class JwtConfig {

    // ==========================================
    // CLAVE SECRETA JWT
    // ==========================================

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${sportx.jwt.secret}") String secret) {

        byte[] bytes = Base64.getDecoder().decode(secret);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                "La clave JWT debe tener al menos 256 bits"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // ==========================================
    // GENERADOR DE TOKENS JWT
    // ==========================================

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {

        return new NimbusJwtEncoder(
            new ImmutableSecret<>(jwtSecretKey)
        );
    }

    // ==========================================
    // DECODIFICADOR Y VALIDACIÓN JWT
    // ==========================================

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            UsuarioJwtValidator usuarioJwtValidator) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        // VALIDAR EMISOR SIN CONVERTIRLO A URI
        OAuth2TokenValidator<Jwt> emisorValidator = jwt -> {

            String issuer = jwt.getClaimAsString("iss");

            if (!"sportx-chile".equals(issuer)) {

                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "Emisor JWT no válido",
                        null
                    )
                );
            }

            return OAuth2TokenValidatorResult.success();
        };

        // VALIDACIONES DE SEGURIDAD
        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                emisorValidator,
                usuarioJwtValidator
            )
        );

        return decoder;
    }
}
