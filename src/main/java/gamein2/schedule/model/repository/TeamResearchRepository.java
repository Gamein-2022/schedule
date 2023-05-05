package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.TeamResearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamResearchRepository extends JpaRepository<TeamResearch, Long> {
    TeamResearch findFirstBySubject_IdOrderByEndTime(Long id);
}