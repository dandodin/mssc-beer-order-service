package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setBeerOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        sendEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Override
    public void processValidationResult(UUID orderId, boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(orderId);
        if (isValid) {
            sendEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            // The object becomes stale because of Hibernate.  Must reload.
            BeerOrder validatedBeerOrder = beerOrderRepository.getOne(orderId);
            sendEvent(validatedBeerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        } else {
            sendEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, boolean allocationError, boolean pendingInventory) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        if (allocationError) {
            sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        } else if (pendingInventory) {
            sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        } else {
            sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            updateAllocatedQty(beerOrderDto);
        }
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if(beerOrderLine.getId() .equals(beerOrderLineDto.getId())){
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });

            beerOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    private void sendEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);
        Message<BeerOrderEventEnum> message =
            MessageBuilder.withPayload(event).setHeader(ORDER_ID_HEADER, beerOrder.getId()).build();
        sm.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(
            sma -> {
                sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getBeerOrderStatus(), null, null, null));
            });
        sm.start();
        return sm;
    }
}
