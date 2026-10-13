
package sportx_api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtAuthenticationConverterConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String rol = jwt.getClaimAsString("rol");

            if (rol == null) {
                return List.of();
            }

            String autoridad = switch (rol) {
                case "Administrador" -> "ROLE_ADMIN";
                case "Vendedor" -> "ROLE_VENDEDOR";
                case "Cliente" -> "ROLE_CLIENTE";
                default -> null;
            };

            if (autoridad == null) {
                return List.of();
            }

            return List.of(
                new SimpleGrantedAuthority(autoridad)
            );
        });

        return converter;
    }
}
