package ru.liga.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.liga.dto.OrderChangeDto;
import ru.liga.service.DeliveryService;

import java.util.Map;

@RestController
@RequestMapping("/courier")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }


    @GetMapping("/deliveries")
    public ResponseEntity<Map<String, Object>> getDeliveriesByStatus(@RequestParam String status,
                                                                     @RequestParam(defaultValue = "0") int pageIndex,
                                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return deliveryService.getDeliveriesByStatus(status, pageIndex, pageSize);
    }


    @PostMapping("/delivery/{id}")
    public ResponseEntity<Void> setDeliveryStatusById(@PathVariable("id") Long id,
                                                      @RequestBody OrderChangeDto orderChange) {
        return deliveryService.setDeliveryStatusByOrderId(id, orderChange);
    }
}
