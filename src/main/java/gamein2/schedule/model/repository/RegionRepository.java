package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region,Long> {
    Region findFirstByRegionId(Long regionId);
}
