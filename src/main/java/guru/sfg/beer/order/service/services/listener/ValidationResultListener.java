package guru.sfg.beer.order.service.services.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @Transactional
    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateBeerOrderResult validateBeerOrderResult) {
        log.info(String.format("Received validation result for id[%s] result[%b]",
            validateBeerOrderResult.getOrderId(), validateBeerOrderResult.getIsValid()));
        beerOrderManager.processValidationResult(validateBeerOrderResult.getOrderId(), validateBeerOrderResult.getIsValid());
    }
}
