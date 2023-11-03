package ru.liga.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {

    @RabbitListener(queues = "updatesToCustomer")
    public void processOrderQueue(String statusUpdate) {
        log.info(statusUpdate);
    }
}
