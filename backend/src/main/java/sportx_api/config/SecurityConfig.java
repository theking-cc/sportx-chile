
package sportx_api.config;

import java.util.List;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter
    ) throws Exception {

        http
            .cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()

                // REGISTRO Y LOGIN PÚBLICOS
                .requestMatchers(
                    "/api/auth/registro",
                    "/api/auth/login"
                ).permitAll()

                // CATÁLOGO PÚBLICO
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/productos",
                    "/api/productos/*",
                    "/api/categorias",
                    "/api/categorias/*",
                    "/api/carrito", 
                    "/api/carrito/**"
                ).permitAll()

                // CREAR Y MODIFICAR PRODUCTOS
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/productos"
                ).hasAnyRole("ADMIN", "VENDEDOR", "CLIENTE")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/productos/*"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // ELIMINAR PRODUCTOS
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/productos/*"
                ).hasRole("ADMIN")

                // ADMINISTRAR CATEGORÍAS
                .requestMatchers(
                    "/api/categorias",
                    "/api/categorias/**"
                ).hasRole("ADMIN")

                .anyRequest().denyAll()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)
                )
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
        ));

        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH",
            "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
            "Content-Type",
            "Accept",
            "Authorization"
        ));

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);

        return source;
    }
}
