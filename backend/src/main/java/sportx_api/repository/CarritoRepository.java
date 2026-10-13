
package sportx_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Carrito;

public interface CarritoRepository
        extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByIdUsuario(Long idUsuario);
}
