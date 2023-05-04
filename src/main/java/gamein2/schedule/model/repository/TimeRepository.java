package gamein2.schedule.model.repository;

import gamein2.schedule.model.entinty.Time;
import org.springframework.data.repository.CrudRepository;

public interface TimeRepository extends CrudRepository<Time,Long> {
}
