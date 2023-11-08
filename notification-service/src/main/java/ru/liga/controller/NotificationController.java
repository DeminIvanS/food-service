package ru.liga.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.liga.rabbit.service.RabbitMQProducerServiceImpl;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final RabbitMQProducerServiceImpl rabbitMQProducerService;

    public NotificationController(RabbitMQProducerServiceImpl rabbitMQProducerService) {
        this.rabbitMQProducerService = rabbitMQProducerService;
    }

    @PostMapping("/order")
    public ResponseEntity<String> sendNewOrderToKitchen(@RequestBody String message) {
        rabbitMQProducerService.sendMessage(message, "new.order");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delivery")
    public ResponseEntity<String> sendNewOrderToDelivery(@RequestBody String message) {
        rabbitMQProducerService.sendMessage(message, "new.delivery");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<String> sendStatusUpdateToCustomer(@RequestBody String message) {
        rabbitMQProducerService.sendMessage(message, "order.status.update");
        return ResponseEntity.ok().build();
    }
}
