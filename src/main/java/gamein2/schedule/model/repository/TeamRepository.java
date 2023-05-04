package gamein2.schedule.model.repository;

import gamein2.schedule.model.entinty.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query(value = "SELECT COUNT(*) FROM Team")
    int getTeamsCount();

    @Query("SELECT COUNT  (*) FROM Team ")
    Integer getCount();

}
