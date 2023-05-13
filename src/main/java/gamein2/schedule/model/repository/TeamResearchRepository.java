package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.TeamResearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TeamResearchRepository extends JpaRepository<TeamResearch, Long> {
    TeamResearch findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(Long id, LocalDateTime now);
    Boolean existsByTeam_IdAndSubject_Id(Long teamId, Long subjectId);
    List<TeamResearch> findAllBySubject_IdAndEndTimeBefore(Long subjectId, LocalDateTime now);
    @Query(value = "SELECT * FROM team_researches AS t WHERE " +
            "t.subject_id = :subjectId AND t.begin_time + (t.end_time - t.begin_time) / 2  < :now ORDER BY t.end_time",
            nativeQuery = true)
    List<TeamResearch> findFirstResearch(Long subjectId, LocalDateTime now);
    @Query(value = "SELECT COUNT(*) FROM team_researches AS t WHERE " +
            "t.subject_id = :subjectId AND t.begin_time + (t.end_time - t.begin_time) / 2 < :now", nativeQuery = true)
    Long getResearchCount(Long subjectId, LocalDateTime now);
}