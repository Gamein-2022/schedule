package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order AS o WHERE o.product.minPrice = o.unitPrice AND o.acceptDate IS NULL AND o.cancelled " +
            "= FALSE AND o.submitDate < :time")
    List<Order> allConvincingOrders(LocalDateTime time);
}
