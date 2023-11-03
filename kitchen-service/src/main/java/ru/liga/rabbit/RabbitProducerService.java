package ru.liga.rabbit;

public interface RabbitProducerService {
    void sendMessage(String message, String routingKey);
}
