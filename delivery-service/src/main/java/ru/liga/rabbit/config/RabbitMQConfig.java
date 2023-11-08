package ru.liga.rabbit.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Declarables routeQueueFromDeliveryService() {
        Queue queueDirectFirst = new Queue("courierAppointments", false);
        Queue queueDirectSecond = new Queue("deliveryStatusUpdates", false);
        DirectExchange directExchange = new DirectExchange("directExchange");

        return new Declarables(queueDirectFirst, queueDirectSecond, directExchange,
                BindingBuilder.bind(queueDirectFirst).to(directExchange).with("courier.appointment"),
                BindingBuilder.bind(queueDirectSecond).to(directExchange).with("delivery.status.update"));
    }
}
