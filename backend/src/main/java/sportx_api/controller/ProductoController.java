
package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.ProductoRequest;
import sportx_api.dto.ProductoResponse;
import sportx_api.service.ProductoService;

@Tag(name = "Productos", description = "Catálogo de productos deportivos.")
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    // Constructor sin Lombok
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET - LISTAR PRODUCTOS
    @Operation(summary = "Listar productos", description = "Público.")
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {

        return ResponseEntity.ok(productoService.listar());
    }

    // GET - BUSCAR POR ID
    @Operation(summary = "Detalle de un producto", description = "Público.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
            productoService.buscarPorId(id)
        );
    }

    // POST - CREAR PRODUCTO
    @Operation(summary = "Crear producto", description = "Rol: Administrador.")
    @PostMapping
    public ResponseEntity<ProductoResponse> guardar(
            @Valid @RequestBody ProductoRequest datos) {

        ProductoResponse nuevo = productoService.guardar(datos);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(nuevo);
    }

    // PUT - ACTUALIZAR PRODUCTO
    @Operation(summary = "Actualizar producto", description = "Rol: Administrador.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest datos) {

        return ResponseEntity.ok(
            productoService.actualizar(id, datos)
        );
    }

    // DELETE - DESACTIVAR PRODUCTO
    @Operation(summary = "Eliminar producto", description = "Rol: Administrador.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
