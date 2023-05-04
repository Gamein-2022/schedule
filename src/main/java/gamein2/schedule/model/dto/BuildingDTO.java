package gamein2.schedule.model.dto;

import gamein2.schedule.model.enums.BuildingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildingDTO {
    private long id;
    private BuildingType type;
    private boolean upgraded;
    private Byte ground;
}
