package ru.liga.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitProducerServiceImpl implements RabbitProducerService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message, String routingKey) {
        rabbitTemplate.convertAndSend("directExchange", routingKey, message);
        log.info("Message: " + message + " was been sent with key " + routingKey);
    }
}
