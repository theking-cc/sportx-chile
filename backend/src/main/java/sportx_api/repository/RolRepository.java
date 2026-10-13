package sportx_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombreIgnoreCase(String nombre);
}
