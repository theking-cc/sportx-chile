package sportx_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.MensajeContacto;

public interface MensajeContactoRepository
        extends JpaRepository<MensajeContacto, Long> {

    List<MensajeContacto> findAllByOrderByFechaCreacionDesc();
}
