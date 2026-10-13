
package sportx_api.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.AuthResponse;
import sportx_api.dto.LoginRequest;
import sportx_api.dto.RegistroRequest;

import sportx_api.model.Rol;
import sportx_api.model.Usuario;

import sportx_api.repository.ComunaRepository;
import sportx_api.repository.RolRepository;
import sportx_api.repository.UsuarioRepository;

import sportx_api.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ComunaRepository comunaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // CONSTRUCTOR SIN LOMBOK
    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            ComunaRepository comunaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.comunaRepository = comunaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================================
    // 1. REGISTRAR CLIENTE
    // =========================================
    @Transactional
    public AuthResponse registrar(RegistroRequest datos) {

        String email = datos.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String rut = datos.rut()
                .trim()
                .toUpperCase(Locale.ROOT);

        // VERIFICAR CORREO DUPLICADO
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo ya está registrado"
            );
        }

        // VERIFICAR RUT DUPLICADO
        if (usuarioRepository.existsByRut(rut)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El RUT ya está registrado"
            );
        }

        // VERIFICAR COMUNA
        if (!comunaRepository.existsById(datos.idComuna())) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La comuna seleccionada no existe"
            );
        }

        // ASIGNAR AUTOMÁTICAMENTE ROL CLIENTE
        Rol rolCliente = rolRepository
            .findByNombreIgnoreCase("Cliente")
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se encontró el rol Cliente"
            ));

        // CREAR USUARIO
        Usuario usuario = new Usuario();

        usuario.setRut(rut);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(email);

        // CIFRAR CONTRASEÑA CON BCRYPT
        usuario.setPasswordHash(
            passwordEncoder.encode(datos.password())
        );

        usuario.setRol(rolCliente);
        usuario.setIdComuna(datos.idComuna());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setActivo(true);

        // GUARDAR EN MARIADB
        Usuario guardado = usuarioRepository.save(usuario);

        // DEVOLVER DATOS SIN CONTRASEÑA
        return convertir(guardado);
    }

    // =========================================
    // 2. INICIAR SESIÓN CON JWT
    // =========================================
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest datos) {

        String email = datos.email().trim();

        // BUSCAR USUARIO POR CORREO
        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
            ));

        // COMPROBAR QUE ESTÉ ACTIVO
        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
            );
        }

        // VERIFICAR CONTRASEÑA CON BCRYPT
        if (!passwordEncoder.matches(
                datos.password(),
                usuario.getPasswordHash())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
            );
        }

        // GENERAR TOKEN JWT
        String token = jwtService.generarToken(usuario);

        // DEVOLVER USUARIO Y TOKEN
        return new AuthResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().getNombre(),
            token
        );
    }

    // =========================================
    // 3. CONVERTIR USUARIO A RESPUESTA
    // =========================================
    private AuthResponse convertir(Usuario usuario) {

        return new AuthResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().getNombre(),
            null
        );
    }
}
