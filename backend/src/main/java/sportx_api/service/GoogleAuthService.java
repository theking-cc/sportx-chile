package sportx_api.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.AuthResponse;
import sportx_api.dto.GoogleLoginResponse;
import sportx_api.dto.GoogleRegistroRequest;
import sportx_api.model.Rol;
import sportx_api.model.Usuario;
import sportx_api.repository.RolRepository;
import sportx_api.repository.UsuarioRepository;
import sportx_api.security.GoogleTokenVerifier;
import sportx_api.security.GoogleTokenVerifier.GoogleUser;
import sportx_api.security.JwtService;

@Service
public class GoogleAuthService {

    private final GoogleTokenVerifier verifier;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UbicacionService ubicacionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public GoogleAuthService(
            GoogleTokenVerifier verifier,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UbicacionService ubicacionService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.verifier = verifier;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.ubicacionService = ubicacionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================================
    // 1. INICIAR SESIÓN CON GOOGLE
    // =========================================
    @Transactional
    public GoogleLoginResponse login(String credential) {

        GoogleUser google = verifier.verificar(credential);

        // a) Cuenta ya vinculada a este Google
        Usuario usuario = usuarioRepository.findByGoogleSub(google.sub()).orElse(null);

        // b) Cuenta existente con el mismo correo verificado: se vincula
        if (usuario == null) {
            usuario = usuarioRepository.findByEmailIgnoreCase(google.email()).orElse(null);

            if (usuario != null) {
                if (usuario.getGoogleSub() != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este correo ya está vinculado a otra cuenta de Google");
                }
                usuario.setGoogleSub(google.sub());
                usuarioRepository.save(usuario);
            }
        }

        // c) No existe: el frontend debe completar el registro
        if (usuario == null) {
            return new GoogleLoginResponse(
                true, google.email(), google.nombre(), google.apellidos(), null);
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Tu cuenta está desactivada");
        }

        return new GoogleLoginResponse(
            false, usuario.getEmail(), usuario.getNombre(), usuario.getApellidos(),
            sesion(usuario));
    }

    // =========================================
    // 2. COMPLETAR REGISTRO (cuenta nueva desde Google)
    // =========================================
    @Transactional
    public AuthResponse registrar(GoogleRegistroRequest datos) {

        GoogleUser google = verifier.verificar(datos.credential());

        if (usuarioRepository.findByGoogleSub(google.sub()).isPresent()
                || usuarioRepository.existsByEmailIgnoreCase(google.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe una cuenta con este correo. Inicia sesión con Google.");
        }

        String rut = datos.rut().trim().toUpperCase(Locale.ROOT);

        if (usuarioRepository.existsByRut(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El RUT ya está registrado");
        }

        ubicacionService.buscarComuna(datos.idComuna());

        Rol cliente = rolRepository.findByNombreIgnoreCase("Cliente")
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "No se encontró el rol Cliente"));

        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(google.email());
        usuario.setGoogleSub(google.sub());
        // La columna exige un hash: se guarda uno aleatorio que nadie conoce.
        // El usuario entra con Google (o define una contraseña desde su perfil).
        usuario.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setRol(cliente);
        usuario.setIdComuna(datos.idComuna());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setActivo(true);

        return sesion(usuarioRepository.save(usuario));
    }

    private AuthResponse sesion(Usuario usuario) {
        return new AuthResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().getNombre(),
            jwtService.generarToken(usuario));
    }
}
