package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.BuildingInfo;
import gamein2.schedule.model.enums.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BuildingInfoRepository extends JpaRepository<BuildingInfo, BuildingType> {
}
