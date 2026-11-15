package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import sportx_api.dto.ActualizarCantidadRequest;
import sportx_api.dto.AgregarCarritoRequest;
import sportx_api.dto.CarritoResponse;
import sportx_api.service.CarritoService;

@Tag(name = "Carrito", description = "Carrito persistente del cliente autenticado.")
@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    private Long obtenerUsuarioId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    @Operation(summary = "Ver mi carrito", description = "Rol: Cliente.")
    @GetMapping
    public ResponseEntity<CarritoResponse> obtener(
            JwtAuthenticationToken auth) {

        return ResponseEntity.ok(
            carritoService.obtener(obtenerUsuarioId(auth))
        );
    }

    @Operation(summary = "Agregar producto al carrito", description = "Rol: Cliente. Valida stock disponible.")
    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregar(
            JwtAuthenticationToken auth,
            @Valid @RequestBody AgregarCarritoRequest datos) {

        return ResponseEntity.ok(
            carritoService.agregar(obtenerUsuarioId(auth), datos)
        );
    }

    @Operation(summary = "Cambiar cantidad de un ítem", description = "Rol: Cliente.")
    @PutMapping("/items/{id}")
    public ResponseEntity<CarritoResponse> actualizar(
            JwtAuthenticationToken auth,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCantidadRequest datos) {

        return ResponseEntity.ok(
            carritoService.actualizar(
                obtenerUsuarioId(auth), id, datos.cantidad())
        );
    }

    @Operation(summary = "Quitar ítem del carrito", description = "Rol: Cliente.")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<CarritoResponse> eliminar(
            JwtAuthenticationToken auth,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            carritoService.eliminar(obtenerUsuarioId(auth), id)
        );
    }
}
