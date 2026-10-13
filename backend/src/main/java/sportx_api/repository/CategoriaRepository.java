package sportx_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import sportx_api.model.Categoria;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    // Listar categorías activas
    List<Categoria> findByActivoTrueOrderByNombreAsc();

    // Buscar categoría activa por ID
    Optional<Categoria> findByIdAndActivoTrue(Long id);

    // Comprobar nombres duplicados
    boolean existsByNombreIgnoreCase(String nombre);

    // Comprobar duplicados al actualizar
    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre, Long id);
}
