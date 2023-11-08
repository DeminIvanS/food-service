package ru.liga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.liga.dto.MessageStatusUpdate;
import ru.liga.dto.OrderMessage;
import ru.liga.rabbit.service.RabbitMQProducerServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KitchenService {

    private final RabbitMQProducerServiceImpl rabbitMQProducerService;
    private final ObjectMapper objectMapper;
    private final Map<UUID, OrderMessage> orders = new HashMap<>();

    private boolean isKitchenAcceptOrder(OrderMessage message) {

        return true;
    }

    private void refund(UUID orderId) {

    }

    private String tryToSerializeStatusUpdateAsString(MessageStatusUpdate dto) {
        String message;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    private String tryToSerializeOrderMessageAsString(OrderMessage order) {
        String message;
        try {
            message = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public String sendMessageOfStatusUpdate(UUID id, String newStatus) {
        MessageStatusUpdate messageDto = new MessageStatusUpdate().setOrderId(id).setNewStatus(newStatus);
        String message = tryToSerializeStatusUpdateAsString(messageDto);
        rabbitMQProducerService.sendMessage(message, "kitchen.status.update");

        return "Status order id=" + id + " changed: " + newStatus;
    }

    public void processNewOrder(String message){
        OrderMessage orderMessage;
        try {
            orderMessage = objectMapper.readValue(message, OrderMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("New order: " + orderMessage);
        UUID orderId = orderMessage.getId();
        if (isKitchenAcceptOrder(orderMessage)) {
            sendMessageOfStatusUpdate(orderId, "kitchen_accepted");
            orders.put(orderId, orderMessage);
        } else {
            sendMessageOfStatusUpdate(orderId, "kitchen_denied");
            refund(orderId);
            sendMessageOfStatusUpdate(orderId, "kitchen_refunded");
            orders.remove(orderId);
        }
    }

    public void sendMessageOfOrderIsReady(UUID id) {
        OrderMessage order = orders.get(id);
        String message = tryToSerializeOrderMessageAsString(order);
        rabbitMQProducerService.sendMessage(message, "new.delivery");
    }
}
