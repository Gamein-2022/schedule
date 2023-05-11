package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.WealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WealthLogRepository extends JpaRepository<WealthLog, Long> {
}
