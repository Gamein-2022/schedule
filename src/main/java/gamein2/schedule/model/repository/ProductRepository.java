package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByLevelAndRegionsContaining(Integer level, Integer region);
    List<Product> findAllByLevelAndRegionsNotContaining(Integer level, Integer region);
    List<Product> findAllByLevelBetween(Integer lower, Integer upper);

    List<Product> findAllByLevelBetweenAndEraBefore(Integer lower,Integer upper,Byte era);
}
