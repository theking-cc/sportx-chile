package sportx_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sportx_api.model.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
