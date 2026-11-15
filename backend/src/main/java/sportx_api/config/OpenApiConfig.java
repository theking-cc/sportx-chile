package sportx_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI 3 de la API REST de SportX Chile.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * Para probar endpoints protegidos: iniciar sesión en POST /api/v1/auth/login,
 * copiar el "token" y pegarlo en el botón "Authorize".
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI sportxOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("SportX Chile API")
                .version("v1")
                .description("API REST del e-commerce deportivo SportX Chile: catálogo, "
                    + "autenticación JWT, carrito, pedidos, pagos Webpay Plus, "
                    + "contacto y administración de usuarios. Rutas versionadas en /api/v1.")
                .contact(new Contact().name("Equipo SportX Chile - DuocUC DSY1104")))
            .components(new Components().addSecuritySchemes(BEARER,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token obtenido en POST /api/v1/auth/login")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
