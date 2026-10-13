
package sportx_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import sportx_api.model.Rol;
import sportx_api.model.Usuario;
import sportx_api.repository.ComunaRepository;
import sportx_api.repository.RolRepository;
import sportx_api.repository.UsuarioRepository;

@Configuration
public class UsuariosInicialesConfig {

    @Bean
    public CommandLineRunner crearUsuariosPrueba(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            ComunaRepository comunaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${sportx.seed.enabled:false}")
            boolean habilitado,
            @Value("${sportx.seed.admin-password:}")
            String adminPassword,
            @Value("${sportx.seed.vendedor-password:}")
            String vendedorPassword) {

        return args -> {

            if (!habilitado) {
                return;
            }

            if (adminPassword.isBlank()
                    || vendedorPassword.isBlank()) {
                throw new IllegalStateException(
                    "Debes configurar las contraseñas de prueba"
                );
            }

            if (!comunaRepository.existsById(1L)) {
                throw new IllegalStateException(
                    "La comuna con ID 1 no existe"
                );
            }

            crearUsuario(
                usuarioRepository,
                rolRepository,
                passwordEncoder,
                "admin@sportx.test",
                "22222222-2",
                "Administrador",
                "SportX",
                "Administrador",
                adminPassword
            );

            crearUsuario(
                usuarioRepository,
                rolRepository,
                passwordEncoder,
                "vendedor@sportx.test",
                "33333333-3",
                "Vendedor",
                "SportX",
                "Vendedor",
                vendedorPassword
            );
        };
    }

    private void crearUsuario(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String rut,
            String nombre,
            String apellidos,
            String nombreRol,
            String password) {

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            System.out.println(
                "Usuario de prueba existente: " + email
            );
            return;
        }

        if (usuarioRepository.existsByRut(rut)) {
            throw new IllegalStateException(
                "El RUT de prueba ya está registrado: " + rut
            );
        }

        Rol rol = rolRepository
            .findByNombreIgnoreCase(nombreRol)
            .orElseThrow(() -> new IllegalStateException(
                "No existe el rol " + nombreRol
            ));

        Usuario usuario = new Usuario();

        usuario.setRut(rut);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setPasswordHash(
            passwordEncoder.encode(password)
        );
        usuario.setRol(rol);
        usuario.setIdComuna(1L);
        usuario.setDireccion("Dirección de prueba SportX");
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        System.out.println(
            "Usuario de prueba creado: " + email
        );
    }
}
