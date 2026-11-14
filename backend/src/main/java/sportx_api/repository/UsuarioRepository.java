
package sportx_api.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sportx_api.model.Usuario;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "rol")
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdConRol(@Param("id") Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRut(String rut);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByRutAndIdNot(String rut, Long id);

    @EntityGraph(attributePaths = "rol")
    List<Usuario> findByActivoTrueOrderByIdAsc();

    long countByRol_NombreAndActivoTrue(String nombreRol);
}
