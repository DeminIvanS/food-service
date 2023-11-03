package ru.liga.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.liga.model.entities.Order;
import ru.liga.service.KitchenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {

    private final ObjectMapper objectMapper;
    private final KitchenService kitchenService;

    @RabbitListener(queues = "courierResponse", ackMode = "MANUAL")
    public void processDeliveryQueue(String message) {
        log.info("Response from courier: <<" + message + ">>");
        kitchenService.processCourierResponse(message);
    }

    @RabbitListener(queues = "orderToKitchen")
    public void processOrderQueue(String orderQueue) {
        log.info("Received order from customer..");
        Order order;
        try {
            order = objectMapper.readValue(orderQueue, Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        kitchenService.processNewOrder(order);
        log.info(order.toString());
    }
}
