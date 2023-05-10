package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.TeamResearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TeamResearchRepository extends JpaRepository<TeamResearch, Long> {
    TeamResearch findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(Long id, LocalDateTime now);
}