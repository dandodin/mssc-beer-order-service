package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidationFailedAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        log.info("in execute");
    }
}