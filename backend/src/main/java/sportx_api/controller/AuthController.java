
package sportx_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sportx_api.dto.AuthResponse;
import sportx_api.dto.LoginRequest;
import sportx_api.dto.RegistroRequest;
import sportx_api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(
            @Valid @RequestBody RegistroRequest datos) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.registrar(datos));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest datos) {

        return ResponseEntity.ok(authService.login(datos));
    }
}
