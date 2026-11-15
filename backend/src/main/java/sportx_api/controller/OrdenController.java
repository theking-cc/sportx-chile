package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.CambiarEstadoRequest;
import sportx_api.dto.CrearOrdenRequest;
import sportx_api.dto.OrdenResponse;
import sportx_api.service.OrdenService;

@Tag(name = "Órdenes (boletas)", description = "Pedidos de compra con su detalle (BOLETA y DETALLE_BOLETA).")
@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    private Long usuarioId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    // CLIENTE: confirma la compra con lo que tiene en su carrito
    @Operation(summary = "Crear orden desde el carrito", description = "Rol: Cliente. Reserva stock (flujo sin pago).")
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(
            JwtAuthenticationToken auth,
            @Valid @RequestBody CrearOrdenRequest datos) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ordenService.crear(usuarioId(auth), datos));
    }

    // CLIENTE: sus pedidos
    @Operation(summary = "Listar mis órdenes", description = "Rol: Cliente.")
    @GetMapping("/mias")
    public ResponseEntity<List<OrdenResponse>> mias(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ordenService.listarPorUsuario(usuarioId(auth)));
    }

    // ADMIN / VENDEDOR: todos los pedidos
    @Operation(summary = "Listar todas las órdenes", description = "Roles: Administrador, Vendedor.")
    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listar() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    // ADMIN / VENDEDOR: detalle de una orden
    @Operation(summary = "Detalle de una orden", description = "Roles: Administrador, Vendedor.")
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtener(id));
    }

    // ADMIN: cambiar estado
    @Operation(summary = "Cambiar estado de una orden", description = "Rol: Administrador. Valida transiciones; cancelar repone stock; no prepara órdenes sin pago.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest datos) {

        return ResponseEntity.ok(ordenService.cambiarEstado(id, datos.estado()));
    }
}
