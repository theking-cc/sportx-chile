package sportx_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import sportx_api.dto.ActualizarCantidadRequest;
import sportx_api.dto.AgregarCarritoRequest;
import sportx_api.dto.CarritoResponse;
import sportx_api.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    private Long obtenerUsuarioId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    @GetMapping
    public ResponseEntity<CarritoResponse> obtener(
            JwtAuthenticationToken auth) {

        return ResponseEntity.ok(
            carritoService.obtener(obtenerUsuarioId(auth))
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregar(
            JwtAuthenticationToken auth,
            @Valid @RequestBody AgregarCarritoRequest datos) {

        return ResponseEntity.ok(
            carritoService.agregar(obtenerUsuarioId(auth), datos)
        );
    }

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

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CarritoResponse> eliminar(
            JwtAuthenticationToken auth,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            carritoService.eliminar(obtenerUsuarioId(auth), id)
        );
    }
}
