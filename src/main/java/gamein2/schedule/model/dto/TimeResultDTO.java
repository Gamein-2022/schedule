package gamein2.schedule.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class TimeResultDTO {
    private Long day;
    private Long month;
    private Long year;
    private Byte era;
    private Long durationMillis;
    private Boolean isGamePaused;

}