package gamein2.schedule.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StorageProductDTO {
    private ProductDTO product;
    private long inStorageAmount;
    private long inRouteAmount;
    private long manufacturingAmount;
    private long blockedAmount;
}
