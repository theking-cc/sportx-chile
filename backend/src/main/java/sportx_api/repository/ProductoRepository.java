
package sportx_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Producto;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "categoria")
    List<Producto>
        findByActivoTrueAndCategoria_ActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    Optional<Producto>
        findByIdAndActivoTrueAndCategoria_ActivoTrue(Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
        String codigo, Long id
    );
}
