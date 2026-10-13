
package sportx_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Comuna;

public interface ComunaRepository
        extends JpaRepository<Comuna, Long> {
}
