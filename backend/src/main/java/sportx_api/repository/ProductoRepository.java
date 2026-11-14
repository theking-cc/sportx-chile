
package sportx_api.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Bloquea la fila del producto mientras se descuenta o repone stock.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
        String codigo, Long id
    );
}
