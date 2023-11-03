package ru.liga.rabbitMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.liga.model.entities.Order;
import ru.liga.service.DeliveryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {

    private final ObjectMapper objectMapper;
    private final DeliveryService deliveryService;

    @RabbitListener(queues = "postNewDelivery")
    public void processDeliveryQueue(String message) {

        log.info("New delivery!");

        Order order;
        try {
            order = objectMapper.readValue(message, Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        deliveryService.processNewDelivery(order);
    }
}
