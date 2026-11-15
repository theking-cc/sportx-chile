package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.ComunaResponse;
import sportx_api.service.UbicacionService;

// Lista pública de comunas (con su región) para registro, perfil y despacho.
@Tag(name = "Comunas", description = "Comunas y regiones para registro y despacho.")
@RestController
@RequestMapping("/api/v1/comunas")
public class ComunaController {

    private final UbicacionService ubicacionService;

    public ComunaController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @Operation(summary = "Listar comunas con su región", description = "Público.")
    @GetMapping
    public ResponseEntity<List<ComunaResponse>> listar() {
        return ResponseEntity.ok(ubicacionService.listarComunas());
    }
}
