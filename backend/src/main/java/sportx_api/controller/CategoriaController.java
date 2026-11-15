
package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

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

import sportx_api.dto.CategoriaResponse;
import sportx_api.model.Categoria;
import sportx_api.service.CategoriaService;

@Tag(name = "Categorías", description = "Categorías del catálogo.")
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // GET - LISTAR CATEGORÍAS
    @Operation(summary = "Listar categorías", description = "Público.")
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {

        return ResponseEntity.ok(categoriaService.listar());
    }

    // GET - BUSCAR POR ID
    @Operation(summary = "Detalle de una categoría", description = "Público.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
            categoriaService.buscarPorId(id)
        );
    }

    // POST - CREAR CATEGORÍA
    @Operation(summary = "Crear categoría", description = "Rol: Administrador.")
    @PostMapping
    public ResponseEntity<CategoriaResponse> guardar(
            @RequestBody Categoria categoria) {

        CategoriaResponse nueva =
            categoriaService.guardar(categoria);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(nueva);
    }

    // PUT - ACTUALIZAR CATEGORÍA
    @Operation(summary = "Actualizar categoría", description = "Rol: Administrador.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable Long id,
            @RequestBody Categoria categoria) {

        return ResponseEntity.ok(
            categoriaService.actualizar(id, categoria)
        );
    }

    // DELETE - DESACTIVAR CATEGORÍA
    @Operation(summary = "Eliminar categoría", description = "Rol: Administrador.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
