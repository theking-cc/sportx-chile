package sportx_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sportx_api.dto.PerfilRequest;
import sportx_api.dto.UsuarioRequest;
import sportx_api.dto.UsuarioResponse;
import sportx_api.service.UsuarioService;

@Tag(name = "Usuarios", description = "Perfil propio y administración de cuentas.")
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private Long usuarioId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    // ---------- Perfil propio (cualquier usuario con sesión) ----------
    @Operation(summary = "Ver mi perfil", description = "Cualquier usuario autenticado.")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> perfil(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(usuarioService.perfil(usuarioId(auth)));
    }

    @Operation(summary = "Actualizar mi perfil", description = "Cualquier usuario autenticado.")
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            JwtAuthenticationToken auth,
            @Valid @RequestBody PerfilRequest datos) {

        return ResponseEntity.ok(
            usuarioService.actualizarPerfil(usuarioId(auth), datos));
    }

    // ---------- Administración (solo Administrador) ----------
    @Operation(summary = "Listar usuarios", description = "Rol: Administrador.")
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(summary = "Crear usuario", description = "Rol: Administrador.")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest datos) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioService.crear(datos));
    }

    @Operation(summary = "Actualizar usuario", description = "Rol: Administrador.")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            JwtAuthenticationToken auth,
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest datos) {

        return ResponseEntity.ok(
            usuarioService.actualizar(id, datos, usuarioId(auth)));
    }

    @Operation(summary = "Desactivar usuario", description = "Rol: Administrador. No borra la fila (conserva sus órdenes).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            JwtAuthenticationToken auth,
            @PathVariable Long id) {

        usuarioService.desactivar(id, usuarioId(auth));
        return ResponseEntity.noContent().build();
    }
}
