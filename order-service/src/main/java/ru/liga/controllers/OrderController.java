package ru.liga.controllers;


import org.springframework.web.bind.annotation.*;
import ru.liga.dto.OrderDto;

@RestController
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/{id}")
    public OrderDto getOrderById(@PathVariable("id") Long id) {
        return new OrderDto()
                .setId(id)
                .setSumm(666.66);
    }
    @PutMapping("/update")
    public String updateOrder(@RequestBody OrderDto orderDto) {

        return "Order update";
    }
    @PostMapping("/create")
    public String createOrder(@RequestBody OrderDto orderDto) {
        return "Create new order";
    }
    public String updateOrderSumm(@PathVariable("id") Long id,
                                  @PathVariable("summ") Double summ,
                                  OrderDto orderDto){
        return "Summ order update";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteOrder(@PathVariable(value = "id", required = true) Long id){
        return "Order with ID = " + id + "deleted";
    }
}
