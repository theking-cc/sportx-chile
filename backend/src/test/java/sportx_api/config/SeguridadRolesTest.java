package sportx_api.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import sportx_api.controller.CarritoController;
import sportx_api.controller.MensajeController;
import sportx_api.controller.OrdenController;
import sportx_api.controller.ProductoController;
import sportx_api.controller.UsuarioController;
import sportx_api.service.CarritoService;
import sportx_api.service.MensajeService;
import sportx_api.service.OrdenService;
import sportx_api.service.ProductoService;
import sportx_api.service.UsuarioService;

/**
 * Matriz de permisos de la pauta (Evaluación 3) contra la configuración real de
 * Spring Security. Los servicios están simulados: solo se prueba quién puede entrar.
 *  - Administrador: acceso total.
 *  - Vendedor: solo ver productos y órdenes (lista y detalle).
 *  - Cliente: solo la tienda (catálogo, carrito, sus pedidos).
 */
@WebMvcTest(controllers = {
    OrdenController.class, ProductoController.class, MensajeController.class,
    UsuarioController.class, CarritoController.class })
@Import({ SecurityConfig.class, JwtAuthenticationConverterConfig.class })
class SeguridadRolesTest {

    @Autowired MockMvc mvc;

    @MockitoBean OrdenService ordenService;
    @MockitoBean ProductoService productoService;
    @MockitoBean MensajeService mensajeService;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean CarritoService carritoService;
    @MockitoBean JwtDecoder jwtDecoder;

    private static final String PRODUCTO = """
        {"codigo":"TEST-1","nombre":"Pelota","descripcion":"x","precio":1000,
         "stock":5,"stockCritico":1,"imagenUrl":null,"categoriaId":2}""";

    private static MockHttpServletRequestBuilder como(String rol, MockHttpServletRequestBuilder req) {
        return req.with(jwt()
            .jwt(j -> j.subject("5").claim("rol", rol))
            .authorities(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @Nested
    @DisplayName("Sin sesión")
    class Anonimo {
        @Test void catalogoEsPublico() throws Exception {
            mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        }
        @Test void ordenesRequierenToken() throws Exception {
            mvc.perform(get("/api/v1/ordenes")).andExpect(status().isUnauthorized());
        }
        @Test void carritoRequiereToken() throws Exception {
            mvc.perform(get("/api/v1/carrito")).andExpect(status().isUnauthorized());
        }
        @Test void rutaSinVersionNoExiste() throws Exception {
            mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Cliente: solo la tienda")
    class Cliente {
        @Test void usaSuCarrito() throws Exception {
            mvc.perform(como("CLIENTE", get("/api/v1/carrito"))).andExpect(status().isOk());
        }
        @Test void veSusPedidos() throws Exception {
            mvc.perform(como("CLIENTE", get("/api/v1/ordenes/mias"))).andExpect(status().isOk());
        }
        @Test void noVeTodasLasOrdenes() throws Exception {
            mvc.perform(como("CLIENTE", get("/api/v1/ordenes"))).andExpect(status().isForbidden());
        }
        @Test void noCreaProductos() throws Exception {
            mvc.perform(como("CLIENTE", post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)))
                .andExpect(status().isForbidden());
        }
        @Test void noAdministraUsuarios() throws Exception {
            mvc.perform(como("CLIENTE", get("/api/v1/usuarios"))).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Vendedor: solo ver productos y órdenes")
    class Vendedor {
        @Test void veProductos() throws Exception {
            mvc.perform(como("VENDEDOR", get("/api/v1/productos"))).andExpect(status().isOk());
            mvc.perform(como("VENDEDOR", get("/api/v1/productos/1"))).andExpect(status().isOk());
        }
        @Test void veOrdenesYDetalle() throws Exception {
            mvc.perform(como("VENDEDOR", get("/api/v1/ordenes"))).andExpect(status().isOk());
            mvc.perform(como("VENDEDOR", get("/api/v1/ordenes/10"))).andExpect(status().isOk());
        }
        @Test void noCreaNiEditaProductos() throws Exception {
            mvc.perform(como("VENDEDOR", post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)))
                .andExpect(status().isForbidden());
            mvc.perform(como("VENDEDOR", put("/api/v1/productos/1")
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)))
                .andExpect(status().isForbidden());
        }
        @Test void noCambiaEstadoDeOrdenes() throws Exception {
            mvc.perform(como("VENDEDOR", patch("/api/v1/ordenes/10/estado")
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Preparando\"}")))
                .andExpect(status().isForbidden());
        }
        @Test void noVeMensajesNiUsuarios() throws Exception {
            mvc.perform(como("VENDEDOR", get("/api/v1/mensajes"))).andExpect(status().isForbidden());
            mvc.perform(como("VENDEDOR", get("/api/v1/usuarios"))).andExpect(status().isForbidden());
        }
        @Test void noCompra() throws Exception {
            mvc.perform(como("VENDEDOR", get("/api/v1/carrito"))).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Administrador: acceso total")
    class Administrador {
        @Test void creaProductos() throws Exception {
            mvc.perform(como("ADMIN", post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)))
                .andExpect(status().isCreated());
        }
        @Test void cambiaEstadoDeOrdenes() throws Exception {
            mvc.perform(como("ADMIN", patch("/api/v1/ordenes/10/estado")
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Preparando\"}")))
                .andExpect(status().isOk());
        }
        @Test void veMensajesYUsuarios() throws Exception {
            mvc.perform(como("ADMIN", get("/api/v1/mensajes"))).andExpect(status().isOk());
            mvc.perform(como("ADMIN", get("/api/v1/usuarios"))).andExpect(status().isOk());
        }
    }
}
