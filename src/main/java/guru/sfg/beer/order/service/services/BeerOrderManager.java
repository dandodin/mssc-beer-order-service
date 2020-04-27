package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void processValidationResult(UUID orderId, boolean isValid);
    void processAllocationResult(BeerOrderDto beerOrderDto, boolean allocationError, boolean pendingInventory);
}
