package sportx_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import sportx_api.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Bloquea el pago mientras se confirma, para que un doble retorno no lo procese dos veces.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pago> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pago> findByOrdenCompra(String ordenCompra);

    Optional<Pago> findFirstByIdOrdenOrderByIdDesc(Long idOrden);
}
