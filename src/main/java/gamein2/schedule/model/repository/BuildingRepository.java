package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findAllByTeamId(Long teamId);
    Optional<Building> findByGroundAndTeam_Id(Byte ground, Long teamId);
}