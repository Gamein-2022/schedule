package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByPeriod(Long period);
}
