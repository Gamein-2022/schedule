package gamein2.schedule.model.entity;

import gamein2.schedule.model.dto.ProductDTO;
import gamein2.schedule.model.enums.ProductGroup;
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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pretty_name", nullable = false, columnDefinition = "character varying(255) default ''")
    private String prettyName;

    @Column(name = "pretty_group")
    private String prettyGroup;

    @ElementCollection()
    @CollectionTable(name = "product_regions",joinColumns = @JoinColumn(name = "product_id"))
    private List<Integer> regions;

    @Column(name = "price")
    private Integer price;

    @Column(name = "available_day")
    private Long availableDay;

    @ManyToOne
    private ResearchSubject RAndD;

    @Column(name = "production_rate")
    private Long productionRate;

    @Column(name = "unit_volume", nullable = false)
    private Integer unitVolume;

    @Column(name = "demand_coefficient")
    private Double demandCoefficient;

    @Column(name = "fixed_cost")
    private Integer fixedCost;

    @Column(name = "variable_cost")
    private Integer variableCost;

    @Column(name = "min_price")
    private Integer minPrice;

    @Column(name = "max_price")
    private Integer maxPrice;

    @Column(name = "era")
    private Byte era;

    @Column(name = "product_group")
    @Enumerated(EnumType.STRING)
    private ProductGroup group;

    public ProductDTO toDTO() {
        return new ProductDTO(id, name, variableCost, level, unitVolume, productionRate, prettyName, prettyGroup, minPrice,
                maxPrice);
    }
}
