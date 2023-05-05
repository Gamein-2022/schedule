package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Log;
import org.springframework.data.repository.CrudRepository;

public interface LogRepository extends CrudRepository<Log,Long> {
}
