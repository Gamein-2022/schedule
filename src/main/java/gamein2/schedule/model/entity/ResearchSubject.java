package gamein2.schedule.model.entity;

import gamein2.schedule.model.dto.ResearchSubjectDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "research_subjects")
public class ResearchSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "available_day", nullable = false)
    private int availableDay;

    @Column(name = "base_price", nullable = false)
    private int basePrice;

    @Column(name = "base_duration", nullable = false)
    private int baseDuration;

    @ManyToOne
    private ResearchSubject durationBound;

    @ManyToOne
    private ResearchSubject parent;

    public ResearchSubjectDTO toDTO() {
        return new ResearchSubjectDTO(name, availableDay);
    }
}
