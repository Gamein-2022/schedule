package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.StorageProduct;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StorageProductRepository extends CrudRepository<StorageProduct,Long> {
    StorageProduct findFirstByTeam_IdAndProduct_Id(Long teamId, Long productId);

    List<StorageProduct> findAllByTeamId(Long teamId);

    Optional<StorageProduct> findFirstByProduct_IdAndTeam_Id(Long productId, Long teamId);
}
