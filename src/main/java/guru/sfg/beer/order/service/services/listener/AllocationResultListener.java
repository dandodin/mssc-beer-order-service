package guru.sfg.beer.order.service.services.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @Transactional
    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE)
    public void listen(AllocateOrderResponse allocateOrderResponse) {
        log.info(String.format("Received allocation result for id[%s] allocation[%b] pendingInv[%b]",
            allocateOrderResponse.getBeerOrder(),
            allocateOrderResponse.getAllocationError(),
            allocateOrderResponse.getPendingInventory()));
        beerOrderManager.processAllocationResult(
            allocateOrderResponse.getBeerOrder(),
            allocateOrderResponse.getAllocationError(),
            allocateOrderResponse.getPendingInventory());
    }
}
