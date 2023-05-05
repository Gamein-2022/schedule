package gamein2.schedule.model.entity;

import gamein2.schedule.model.enums.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;


@DynamicInsert
@Entity
@Table(name = "offers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    @ManyToOne(optional = false)
    private Order order;
    @ManyToOne(optional = false)
    private Team offerer;
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    @Column(name = "accept_date")
    private LocalDateTime acceptDate;
    @Column(name = "declined", nullable = false, columnDefinition = "boolean default false")
    private Boolean declined = false;
    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;
    @Column
    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;
    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;
}
