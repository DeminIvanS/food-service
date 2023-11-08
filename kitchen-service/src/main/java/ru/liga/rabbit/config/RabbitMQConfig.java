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
    public Declarables routeQueueFromKitchenService() {
        Queue queueDirectFirst = new Queue("newDeliveries", false);
        Queue queueDirectSecond = new Queue("kitchenStatusUpdates", false);
        DirectExchange directExchange = new DirectExchange("directExchange");

        return new Declarables(queueDirectFirst, queueDirectSecond, directExchange,
                BindingBuilder.bind(queueDirectFirst).to(directExchange).with("new.delivery"),
                BindingBuilder.bind(queueDirectSecond).to(directExchange).with("kitchen.status.update"));
    }
}
