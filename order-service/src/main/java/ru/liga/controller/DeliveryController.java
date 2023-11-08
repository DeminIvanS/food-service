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

    @Operation(summary = "Get available deliveries")
    @GetMapping
    public ResponseEntity<Collection<OrderMessage>> getAvailableDeliveries() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @Operation(summary = "Accept delivery order")
    @PostMapping("/{id}/take")
    public ResponseEntity<String> acceptDelivery(@PathVariable("id") UUID id) {
        String newStatus = "delivery_picking";
        return ResponseEntity.ok(deliveryService.setDeliveryStatusByOrderId(id, newStatus));
    }

    @Operation(summary = "Order Complete")
    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeDelivery(@PathVariable("id") UUID id) {
        String newStatus = "delivery_complete";
        return ResponseEntity.ok(deliveryService.setDeliveryStatusByOrderId(id, newStatus));
    }
}
