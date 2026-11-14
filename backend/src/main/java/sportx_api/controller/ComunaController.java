package sportx_api.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.ComunaResponse;
import sportx_api.repository.ComunaRepository;

// Lista pública de comunas para el formulario de registro del frontend.
@RestController
@RequestMapping("/api/comunas")
public class ComunaController {

    private final ComunaRepository comunaRepository;

    public ComunaController(ComunaRepository comunaRepository) {
        this.comunaRepository = comunaRepository;
    }

    @GetMapping
    public ResponseEntity<List<ComunaResponse>> listar() {

        List<ComunaResponse> comunas = comunaRepository.findAll()
            .stream()
            .map(c -> new ComunaResponse(c.getId(), c.getIdRegion(), c.getNombre()))
            .sorted(Comparator.comparing(ComunaResponse::nombre))
            .toList();

        return ResponseEntity.ok(comunas);
    }
}
