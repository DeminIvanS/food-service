package ru.liga.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {

    private final RabbitProducerServiceImpl rabbitProducerService;


    @RabbitListener(queues = {"kitchenStatusUpdate", "deliveryStatusUpdate"})
    public void processUpdateQueue(String update) {
        log.info("Status update!");
        rabbitProducerService.sendMessage(update, "order.status.update");
    }

    @RabbitListener(queues = "postNewOrder")
    public void processOrderQueue(String order) {
        log.info("New order!");
        rabbitProducerService.sendMessage(order, "new.order");
    }
}
