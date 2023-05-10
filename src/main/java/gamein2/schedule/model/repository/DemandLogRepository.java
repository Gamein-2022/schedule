package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.DemandLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DemandLogRepository extends JpaRepository<DemandLog, Long> {
}
