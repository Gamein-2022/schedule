package gamein2.schedule.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import gamein2.schedule.model.enums.OrderType;
import javax.persistence.*;
import java.time.LocalDateTime;


@DynamicInsert
@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Column(name = "unit_price", updatable = false, nullable = false)
    private Long unitPrice;

    @Column(name = "submit_date", nullable = false)
    private LocalDateTime submitDate;

    @Column(name = "cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelled = false;

    @Column(name = "accept_date")
    private LocalDateTime acceptDate;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "product_amount", nullable = false)
    private Integer productAmount;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;

    @ManyToOne(optional = false)
    private Team submitter;

    @OneToOne
    private Shipping shipping;

    @ManyToOne
    private Team accepter;
}