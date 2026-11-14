package sportx_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.ComunaResponse;
import sportx_api.service.UbicacionService;

// Lista pública de comunas (con su región) para registro, perfil y despacho.
@RestController
@RequestMapping("/api/comunas")
public class ComunaController {

    private final UbicacionService ubicacionService;

    public ComunaController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    public ResponseEntity<List<ComunaResponse>> listar() {
        return ResponseEntity.ok(ubicacionService.listarComunas());
    }
}
