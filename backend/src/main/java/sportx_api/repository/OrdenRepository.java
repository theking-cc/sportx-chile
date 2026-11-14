package sportx_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @EntityGraph(attributePaths = "detalles")
    List<Orden> findByIdUsuarioOrderByFechaOrdenDesc(Long idUsuario);

    @EntityGraph(attributePaths = "detalles")
    List<Orden> findAllByOrderByFechaOrdenDesc();

    @EntityGraph(attributePaths = "detalles")
    Optional<Orden> findWithDetallesById(Long id);
}
