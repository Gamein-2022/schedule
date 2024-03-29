package gamein2.schedule.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_researches")
public class TeamResearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private ResearchSubject subject;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "paid_amount")
    private int paidAmount;
}