package gamein2.schedule.model.entity;

import gamein2.schedule.model.dto.StorageProductDTO;
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
@Table(name = "storage_products")
public class StorageProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @ManyToOne(optional = false)
    private Product product;

    @Column(name = "in_storage_amount", nullable = false, columnDefinition = "integer default 0")
    private int inStorageAmount = 0;

    @Column(name = "manufacturing_amount", nullable = false, columnDefinition = "integer default 0")
    private int manufacturingAmount = 0;

    @Column(name = "in_route_amount", nullable = false, columnDefinition = "integer default 0")
    private int inRouteAmount = 0;

    @Column(name = "blocked_amount", nullable = false, columnDefinition = "integer default 0")
    private int blockedAmount = 0;

    @Column(name = "sellable_amount", nullable = false,  columnDefinition = "integer default 0")
    private int sellableAmount = 0;

    @ManyToOne(optional = false)
    private Team team;

    /*public StorageProductDTO toDTO() {
        return new StorageProductDTO(
                product.toDTO(),
                inStorageAmount,
                inRouteAmount,
                manufacturingAmount,
                blockedAmount
        );
    }*/
}
