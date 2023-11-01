package ru.liga.controllers;


import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.liga.dto.OrderDto;
import ru.liga.handler.GlobalExceptionHandler;
import ru.liga.service.OrderService;

import java.util.Map;

@Import(GlobalExceptionHandler.class)
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("/all_orders")
    public ResponseEntity<Map<String, Object>> getOrder(@RequestParam(defaultValue = "0") int pageIndex,
                                                        @RequestParam(defaultValue = "15") int pageSize) {
        return orderService.getOrders(pageIndex, pageSize);
    }
    @PutMapping("/update")
    public String updateOrder(@RequestBody OrderDto orderDto) {

        return "Order update";
    }
    @PostMapping("/create")
    public String createOrder(@RequestBody OrderDto orderDto) {
        return "Create new order";
    }

    @PatchMapping("/{id}/{summ}")
    public String updateOrderSumm(@PathVariable("id") Long id,
                                  @PathVariable("summ") Double summ,
                                  OrderDto orderDto){
        return "Summ order update";
    }
    @PatchMapping("/{id}")
    public String updateOrderSumm2(@PathVariable(value = "id", required = true) Long id,
                                   @RequestParam("summ") Double summ) {
        return "Summ order update";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteOrder(@PathVariable(value = "id", required = true) Long id){
        return "Order with ID = " + id + "deleted";
    }
}
