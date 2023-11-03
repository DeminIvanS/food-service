package ru.liga.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.liga.dto.OrderChangeDto;

@FeignClient(name = "delivery-service", url = "http://localhost:8080")
public interface DeliveryClient {

    @PostMapping("/courier/delivery/{id}")
    ResponseEntity<String> setOrderStatusById(@PathVariable("id") Long id,
                                              @RequestBody OrderChangeDto orderChange);
}
