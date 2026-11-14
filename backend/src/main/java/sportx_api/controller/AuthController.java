
package sportx_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sportx_api.dto.AuthResponse;
import sportx_api.dto.GoogleLoginRequest;
import sportx_api.dto.GoogleLoginResponse;
import sportx_api.dto.GoogleRegistroRequest;
import sportx_api.service.GoogleAuthService;
import sportx_api.dto.LoginRequest;
import sportx_api.dto.RegistroRequest;
import sportx_api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthController(
            AuthService authService,
            GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    // Inicio de sesión con Google (ID token de Google Identity Services)
    @PostMapping("/google")
    public ResponseEntity<GoogleLoginResponse> google(
            @Valid @RequestBody GoogleLoginRequest datos) {

        return ResponseEntity.ok(googleAuthService.login(datos.credential()));
    }

    // Completa la cuenta nueva con los datos que Google no entrega
    @PostMapping("/google/registro")
    public ResponseEntity<AuthResponse> googleRegistro(
            @Valid @RequestBody GoogleRegistroRequest datos) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(googleAuthService.registrar(datos));
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
