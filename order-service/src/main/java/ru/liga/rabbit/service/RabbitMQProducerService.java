package ru.liga.rabbit.service;

public interface RabbitMQProducerService {

    void sendMessage(String message, String routingKey);
}
