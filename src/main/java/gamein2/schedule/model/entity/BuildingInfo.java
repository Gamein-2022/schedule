package gamein2.schedule.model.entity;

import gamein2.schedule.model.enums.BuildingType;
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
@Table(name = "buildings_info")
public class BuildingInfo {
    @Id
    @Column(name = "type", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private BuildingType type;

    @Column(name = "build_price")
    private int buildPrice;

    @Column(name = "upgrade_price")
    private int upgradePrice;

    @Column(name = "base_line_count")
    private int baseLineCount;

    @Column(name = "upgraded_line_count")
    private int upgradeLineCount;
}
