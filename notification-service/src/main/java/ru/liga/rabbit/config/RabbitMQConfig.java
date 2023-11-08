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
    public Declarables routeQueuesFromNotificationService() {
        Queue queueDirectFirst = new Queue("newOrdersToKitchen", false);
        Queue queueDirectSecond = new Queue("statusUpdates", false);
        Queue queueDirectThird = new Queue("newDeliveriesToDelivery", false);
        Queue queueDirectFourth = new Queue("courierAppointmentsToOrder", false);
        DirectExchange directExchange = new DirectExchange("directExchange");

        return new Declarables(queueDirectFirst, queueDirectSecond, queueDirectThird, queueDirectFourth, directExchange,
                BindingBuilder.bind(queueDirectFirst).to(directExchange).with("new.order"),
                BindingBuilder.bind(queueDirectSecond).to(directExchange).with("order.status.update"),
                BindingBuilder.bind(queueDirectThird).to(directExchange).with("new.delivery"),
                BindingBuilder.bind(queueDirectFourth).to(directExchange).with("courier.appointment"));
    }
}