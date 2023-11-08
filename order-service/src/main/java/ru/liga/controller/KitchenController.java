package ru.liga.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.liga.service.KitchenService;

import java.util.UUID;

@RestController
@RequestMapping("/kitchen")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @Operation(summary = "Order accept")
    @PostMapping("/{id}/accept")
    public ResponseEntity<String> acceptOrder(@PathVariable("id") UUID id) {
        String newStatus = "kitchen_accepted";
        return ResponseEntity.ok(kitchenService.sendMessageOfStatusUpdate(id, newStatus));
    }

    @Operation(summary = "Order decline")
    @PostMapping("/{id}/decline")
    public ResponseEntity<String> declineOrder(@PathVariable("id") UUID id) {
        String newStatus = "kitchen_denied";
        return ResponseEntity.ok(kitchenService.sendMessageOfStatusUpdate(id, newStatus));
    }

    @Operation(summary = "Order finish")
    @PostMapping("/{id}/ready")
    public ResponseEntity<String> finishOrder(@PathVariable("id") UUID id) {
        String newStatus = "delivery_pending";
        kitchenService.sendMessageOfOrderIsReady(id);
        return ResponseEntity.ok(kitchenService.sendMessageOfStatusUpdate(id, newStatus));
    }

}
