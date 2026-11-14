package sportx_api.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.ComunaResponse;
import sportx_api.dto.PerfilRequest;
import sportx_api.dto.UsuarioRequest;
import sportx_api.dto.UsuarioResponse;
import sportx_api.model.Rol;
import sportx_api.model.Usuario;
import sportx_api.repository.RolRepository;
import sportx_api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String ADMIN = "Administrador";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UbicacionService ubicacionService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UbicacionService ubicacionService,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.ubicacionService = ubicacionService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // ADMINISTRACIÓN (solo Administrador)
    // =========================================
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        Map<Long, ComunaResponse> comunas = ubicacionService.mapaComunas();
        return usuarioRepository.findByActivoTrueOrderByIdAsc()
            .stream()
            .map(u -> convertir(u, comunas.get(u.getIdComuna())))
            .toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest datos) {

        validarPassword(datos.password(), true);

        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        aplicarDatosAdmin(usuario, datos, null);
        usuario.setPasswordHash(passwordEncoder.encode(datos.password()));

        return convertir(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest datos, Long actorId) {

        Usuario usuario = buscarActivo(id);

        boolean eraAdmin = ADMIN.equals(usuario.getRol().getNombre());
        boolean seraAdmin = ADMIN.equalsIgnoreCase(datos.rol().trim());

        if (eraAdmin && !seraAdmin) {
            if (id.equals(actorId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes quitarte el rol de administrador");
            }
            validarQuedaOtroAdmin();
        }

        aplicarDatosAdmin(usuario, datos, id);

        if (datos.password() != null && !datos.password().isBlank()) {
            validarPassword(datos.password(), true);
            usuario.setPasswordHash(passwordEncoder.encode(datos.password()));
        }

        return convertir(usuarioRepository.save(usuario));
    }

    // No se borra la fila (tiene pedidos asociados): se desactiva.
    @Transactional
    public void desactivar(Long id, Long actorId) {

        if (id.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No puedes eliminar tu propia cuenta");
        }

        Usuario usuario = buscarActivo(id);

        if (ADMIN.equals(usuario.getRol().getNombre())) {
            validarQuedaOtroAdmin();
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    // =========================================
    // PERFIL PROPIO
    // =========================================
    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long id) {
        return convertir(buscarActivo(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, PerfilRequest datos) {

        Usuario usuario = buscarActivo(id);
        String email = normalizarEmail(datos.email());

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El correo ya está registrado");
        }

        ubicacionService.buscarComuna(datos.idComuna());

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(email);
        usuario.setIdComuna(datos.idComuna());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setFechaNacimiento(datos.fechaNacimiento());

        if (datos.password() != null && !datos.password().isBlank()) {
            validarPassword(datos.password(), true);
            usuario.setPasswordHash(passwordEncoder.encode(datos.password()));
        }

        return convertir(usuarioRepository.save(usuario));
    }

    // =========================================
    // AUXILIARES
    // =========================================
    private void aplicarDatosAdmin(Usuario usuario, UsuarioRequest datos, Long id) {

        String email = normalizarEmail(datos.email());
        String rut = datos.rut().trim().toUpperCase(Locale.ROOT);

        boolean emailDuplicado = id == null
            ? usuarioRepository.existsByEmailIgnoreCase(email)
            : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id);

        if (emailDuplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El correo ya está registrado");
        }

        boolean rutDuplicado = id == null
            ? usuarioRepository.existsByRut(rut)
            : usuarioRepository.existsByRutAndIdNot(rut, id);

        if (rutDuplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El RUT ya está registrado");
        }

        Rol rol = rolRepository.findByNombreIgnoreCase(datos.rol().trim())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Rol inválido"));

        ubicacionService.buscarComuna(datos.idComuna());

        usuario.setRut(rut);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setIdComuna(datos.idComuna());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
    }

    private void validarQuedaOtroAdmin() {
        if (usuarioRepository.countByRol_NombreAndActivoTrue(ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe quedar al menos un administrador");
        }
    }

    private void validarPassword(String password, boolean obligatoria) {
        if ((obligatoria && (password == null || password.isBlank()))
                || (password != null && (password.length() < 8 || password.length() > 72))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La contraseña debe tener al menos 8 caracteres");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario buscarActivo(Long id) {
        Usuario usuario = usuarioRepository.findByIdConRol(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return usuario;
    }

    private UsuarioResponse convertir(Usuario u) {
        ComunaResponse comuna = null;
        try {
            comuna = ubicacionService.buscarComuna(u.getIdComuna());
        } catch (ResponseStatusException ex) {
            // comuna inexistente: se devuelve sin nombre
        }
        return convertir(u, comuna);
    }

    private UsuarioResponse convertir(Usuario u, ComunaResponse comuna) {
        return new UsuarioResponse(
            u.getId(),
            u.getRut(),
            u.getNombre(),
            u.getApellidos(),
            u.getEmail(),
            u.getRol().getNombre(),
            u.getIdComuna(),
            comuna == null ? null : comuna.nombre(),
            comuna == null ? null : comuna.region(),
            u.getDireccion(),
            u.getFechaNacimiento()
        );
    }
}
