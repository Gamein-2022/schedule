package gamein2.schedule.model.entity;

import gamein2.schedule.model.dto.ShippingDTO;
import gamein2.schedule.model.enums.ShippingMethod;
import gamein2.schedule.model.enums.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

import static gamein2.schedule.util.TeamUtil.calculateAvailableSpace;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shipping")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "method", nullable = false)
    private ShippingMethod method;

    @Column(name = "source_region", nullable = false)
    private Integer sourceRegion;

    @Column(name = "departure_time", nullable = false)
    private Date departureTime;

    @Column(name = "arrival_time", nullable = false)
    private Date arrivalTime;

    @Column(name = "status")
    private ShippingStatus status;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "amount", nullable = false)
    private int amount;

   /* public ShippingDTO toDTO(Time time) {
        return new ShippingDTO(
                id, sourceRegion, team.getId(), method, status, departureTime, arrivalTime, new Date(),
                product.toDTO(), amount,
                status == ShippingStatus.IN_QUEUE && calculateAvailableSpace(team) >= product.getUnitVolume() * amount
        );
    }*/
}