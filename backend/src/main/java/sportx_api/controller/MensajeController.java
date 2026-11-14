package sportx_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.MensajeRequest;
import sportx_api.dto.MensajeResponse;
import sportx_api.service.MensajeService;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    // Público: formulario de contacto. Si hay sesión, se asocia al usuario.
    @PostMapping
    public ResponseEntity<MensajeResponse> enviar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MensajeRequest datos) {

        Long usuarioId = jwt == null ? null : Long.valueOf(jwt.getSubject());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mensajeService.guardar(datos, usuarioId));
    }

    // ADMIN / VENDEDOR
    @GetMapping
    public ResponseEntity<List<MensajeResponse>> listar() {
        return ResponseEntity.ok(mensajeService.listar());
    }
}
