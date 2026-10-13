
package sportx_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.model.Usuario;
import sportx_api.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/test/usuarios")
public class UsuarioTestController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioTestController(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<UsuarioTestResponse> listar() {

        return usuarioRepository.findAll()
            .stream()
            .map(usuario -> new UsuarioTestResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
            ))
            .toList();
    }

    public record UsuarioTestResponse(
        Long id,
        String nombre,
        String email,
        String rol
    ) {
    }
}
