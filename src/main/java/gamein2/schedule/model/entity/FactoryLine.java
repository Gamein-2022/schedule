package gamein2.schedule.model.entity;

import gamein2.schedule.model.enums.LineStatus;
import gamein2.schedule.model.enums.LineType;
import gamein2.schedule.model.enums.ProductGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "factory_line")
public class FactoryLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "team_id")
    private Long teamId;


    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LineType type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LineStatus status;

    @OneToOne
    private Product product;

    @Column(name = "count")
    private Integer count;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "product_group")
    @Enumerated(EnumType.STRING)
    private ProductGroup group;

    @Column(name = "initiation_date")
    private LocalDateTime initiationDate;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Building building;
}
