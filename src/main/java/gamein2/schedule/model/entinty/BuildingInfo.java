package gamein2.schedule.model.entinty;

import gamein2.schedule.model.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildingInfo {
    private int baseLineCount;
    private int upgradedLineCount;
    private int buildPrice;
    private int upgradePrice;
    private boolean upgradable;

    public static BuildingInfo getInfo(BuildingType type) {
        switch (type) {
            case PRODUCTION_FACTORY:
                return new BuildingInfo(
                        2, 4, 110_000_000, 100_000_000, true
                );
            case ASSEMBLY_FACTORY:
                return new BuildingInfo(
                        3, 4, 110_000_000, 100_000_000, true
                );
            default:
                return new BuildingInfo(
                        1, 0, 110_000_000, 0, false
                );
        }
    }
}
