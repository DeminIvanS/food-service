package ru.liga.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.liga.dto.OrderMessage;
import ru.liga.service.DeliveryService;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Operation(summary = "Get orders")
    @GetMapping
    public ResponseEntity<Collection<OrderMessage>> getAvailableDeliveries() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @Operation(summary = "accept order")
    @PostMapping("/{id}/accept")
    public ResponseEntity<String> setDeliveryAccept(@PathVariable("id") UUID id) {
        String newStatus = "delivery_accept";
        return ResponseEntity.ok(deliveryService.setDeliveryStatusByOrderId(id, newStatus));
    }

    @Operation(summary = "delivery complete")
    @PostMapping("/{id}/complete")
    public ResponseEntity<String> setDeliveryComplete(@PathVariable("id") UUID id) {
        String newStatus = "delivery_complete";
        return ResponseEntity.ok(deliveryService.setDeliveryStatusByOrderId(id, newStatus));
    }
}
