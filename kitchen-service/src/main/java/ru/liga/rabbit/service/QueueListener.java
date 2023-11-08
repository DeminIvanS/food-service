package ru.liga.rabbit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.liga.service.KitchenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {
    private final KitchenService kitchenService;

    @RabbitListener(queues = "postNewDelivery")
    public void processDeliveryQueue(String message) {

        log.info("New delivery!");
        kitchenService.processNewOrder(message);
    }
}
