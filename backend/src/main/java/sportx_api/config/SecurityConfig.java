
package sportx_api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

import jakarta.servlet.DispatcherType;

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

                // DOCUMENTACIÓN SWAGGER / OPENAPI (pública, solo lectura)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()

                // LOGIN Y REGISTRO PÚBLICOS
                .requestMatchers(
                    "/api/v1/auth/registro",
                    "/api/v1/auth/login",
                    "/api/v1/auth/google",
                    "/api/v1/auth/google/registro"
                ).permitAll()

                // CONSULTAR PRODUCTOS Y CATEGORÍAS
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/productos",
                    "/api/v1/productos/*",
                    "/api/v1/categorias",
                    "/api/v1/categorias/*",
                    "/api/v1/comunas"
                ).permitAll()

                // FORMULARIO DE CONTACTO (público)
                .requestMatchers(HttpMethod.POST, "/api/v1/mensajes").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/mensajes").hasRole("ADMIN")

                // PAGOS WEBPAY: iniciar solo clientes; retorno y simulador son públicos
                .requestMatchers(HttpMethod.POST, "/api/v1/pagos/webpay/iniciar").hasRole("CLIENTE")
                .requestMatchers("/api/v1/pagos/webpay/retorno", "/api/v1/pagos/simulador").permitAll()

                // PEDIDOS
                .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/mias").hasRole("CLIENTE")
                // Vendedor: solo VER la lista de órdenes y su detalle (pauta Evaluación 3)
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes", "/api/v1/ordenes/*")
                    .hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/estado").hasRole("ADMIN")

                // PERFIL PROPIO (cualquier usuario autenticado)
                .requestMatchers("/api/v1/usuarios/me").authenticated()

                // ADMINISTRACIÓN DE USUARIOS
                .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMIN")

                // CARRITO: SOLO CLIENTES
                .requestMatchers(
                    "/api/v1/carrito",
                    "/api/v1/carrito/**"
                ).hasRole("CLIENTE")

                // CREAR Y MODIFICAR PRODUCTOS: solo Administrador
                // (el Vendedor solo visualiza productos, que son de lectura pública)
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/productos"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/productos/*"
                ).hasRole("ADMIN")

                // ELIMINAR PRODUCTOS
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/productos/*"
                ).hasRole("ADMIN")

                // ADMINISTRAR CATEGORÍAS
                .requestMatchers(
                    "/api/v1/categorias",
                    "/api/v1/categorias/**"
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
    public UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${sportx.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
            List<String> origenes) {

        CorsConfiguration config = new CorsConfiguration();

        // Patrones: admite comodines como https://*.floot.app (vista previa y
        // publicación del frontend en Floot) además de orígenes exactos.
        config.setAllowedOriginPatterns(origenes.stream().map(String::trim).toList());

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
