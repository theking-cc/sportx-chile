
package sportx_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.DetalleCarrito;

public interface DetalleCarritoRepository
        extends JpaRepository<DetalleCarrito, Long> {

    List<DetalleCarrito> findByIdCarritoOrderByIdAsc(Long idCarrito);

    Optional<DetalleCarrito> findByIdCarritoAndIdProducto(
            Long idCarrito,
            Long idProducto
    );

    Optional<DetalleCarrito> findByIdAndIdCarrito(
            Long id,
            Long idCarrito
    );

    void deleteByIdCarrito(Long idCarrito);
}
