package gamein2.schedule.model.entity;

import gamein2.schedule.model.dto.BuildingDTO;
import gamein2.schedule.model.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "buildings", uniqueConstraints={
        @UniqueConstraint(columnNames = {"ground", "team_id"})
})
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Team team;

    @Column(name = "type")
    private BuildingType type;

    @Column(name = "upgraded", columnDefinition = "boolean default false")
    private boolean upgraded = false;

    @Column(name = "ground", nullable = false)
    private Byte ground;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<FactoryLine> lines;

    public BuildingDTO toDTO() {
        return new BuildingDTO(
                id,
                type,
                upgraded,
                ground
        );
    }
}
