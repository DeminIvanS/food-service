package ru.liga.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.liga.service.OrderService;
import ru.liga.dto.OrderActionDto;
import ru.liga.dto.OrderDto;
import ru.liga.request.OrderRequest;
import ru.liga.response.ResponseDto;

import java.util.UUID;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "get orders", description = "getting orders")
    @GetMapping
    public ResponseEntity<ResponseDto<OrderDto>> getOrders(@RequestParam(defaultValue = "0")
                                                            int pageIndex,
                                                           @RequestParam(defaultValue = "10")
                                                            int pageSize) {
        return orderService.getOrders(pageIndex, pageSize);
    }

    @Operation(summary = "get order from ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable("id")
                                                     @Parameter(description = "Order Id", required = true)
                                                     UUID id) {
        return orderService.getOrderById(id);
    }

    @Operation(summary = "Create order")
    @PostMapping
    public ResponseEntity<String> postNewOrder(@RequestBody OrderRequest order) {
        return orderService.postNewOrder(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateOrderStatus(@PathVariable("id") UUID id,
                                                    @RequestBody OrderActionDto orderAction) {
        String newStatus = orderAction.getOrderAction();
        String response = orderService.updateOrderStatusById(id, newStatus);
        return ResponseEntity.ok(response);
    }
}