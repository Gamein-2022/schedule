package gamein2.schedule.model.repository;

import gamein2.schedule.model.dto.RegionDTO;
import gamein2.schedule.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query(value = "SELECT COUNT(*) FROM Team")
    int getTeamsCount();

    @Query("SELECT COUNT  (*) FROM Team ")
    Integer getCount();

    @Query("select new gamein2.schedule.model.dto.RegionDTO(t.region,count(t))  from Team as t group by t.region")
    List<RegionDTO> getRegionsPopulation();
}
