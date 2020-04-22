package guru.sfg.beer.order.service.services.beer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto {

    private UUID id;
    private Integer version;

    private String beerName;
    private String beerStyle;
    private String upc;
    private BigDecimal price;
    private Integer quantityOnHand;

    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;
}
