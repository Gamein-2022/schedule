package gamein2.schedule.model.repository;

import gamein2.schedule.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order AS o WHERE ((o.type = 'SELL' AND o.product.minPrice = o.unitPrice) OR (o.type = 'BUY'" +
            " AND o.product.maxPrice = o.unitPrice)) AND o" +
            ".acceptDate IS" +
            " NULL AND o.cancelled " +
            "= FALSE AND o.submitDate < :time AND o.id NOT IN (SELECT offer.order.id FROM Offer AS offer WHERE offer" +
            ".cancelled = FALSE) AND o.id " +
            "NOT IN (SELECT o2.id FROM Order AS o2 WHERE o2.product = o.product AND o2.acceptDate IS NULL AND o2.type" +
            " != o.type)")
    List<Order> allConvincingOrders(LocalDateTime time);

    List<Order> findAllBySubmitDateBeforeAndCancelledIsFalseAndAcceptDateIsNull(LocalDateTime tenMinutesAgo);;
}
