package ru.liga.rabbitMQ;

public interface RabbitProducerService {
    void sendMessage(String message, String routingKey);
}
