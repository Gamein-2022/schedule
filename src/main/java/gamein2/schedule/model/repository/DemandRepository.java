package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandRepository extends JpaRepository<Demand, Long> {
}
