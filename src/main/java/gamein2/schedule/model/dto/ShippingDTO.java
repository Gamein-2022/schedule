package gamein2.schedule.model.dto;

import gamein2.schedule.model.enums.ShippingMethod;
import gamein2.schedule.model.enums.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class ShippingDTO {
    private Long id;
    private Integer sourceRegion;
    private Long teamId;
    private ShippingMethod method;
    private ShippingStatus status;
    private Date departureTime;
    private Date arrivalTime;
    private Date currentTime;
    private ProductDTO product;
    private int amount;
    private boolean collectable;
}
