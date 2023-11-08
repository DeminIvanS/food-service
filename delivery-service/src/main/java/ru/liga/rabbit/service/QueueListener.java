package ru.liga.rabbit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.liga.service.DeliveryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {
    private final DeliveryService deliveryService;

    @RabbitListener(queues = "postNewDelivery")
    public void processDeliveryQueue(String message) {

        log.info("New delivery!");
        deliveryService.processNewDelivery(message);
    }
}
