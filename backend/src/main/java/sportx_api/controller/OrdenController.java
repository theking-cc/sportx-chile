package sportx_api.controller;

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

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    private Long usuarioId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    // CLIENTE: confirma la compra con lo que tiene en su carrito
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(
            JwtAuthenticationToken auth,
            @Valid @RequestBody CrearOrdenRequest datos) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ordenService.crear(usuarioId(auth), datos));
    }

    // CLIENTE: sus pedidos
    @GetMapping("/mias")
    public ResponseEntity<List<OrdenResponse>> mias(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ordenService.listarPorUsuario(usuarioId(auth)));
    }

    // ADMIN / VENDEDOR: todos los pedidos
    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listar() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    // ADMIN / VENDEDOR: cambiar estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest datos) {

        return ResponseEntity.ok(ordenService.cambiarEstado(id, datos.estado()));
    }
}
