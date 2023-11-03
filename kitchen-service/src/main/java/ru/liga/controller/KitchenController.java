package ru.liga.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.liga.dto.PriceDto;
import ru.liga.dto.RestaurantMenuItemDto;
import ru.liga.service.KitchenService;

import java.util.Map;

@RestController
@RequestMapping("/restaurant")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>>getOrderByStatus(@RequestParam String status,
                                                               @RequestParam(defaultValue = "0") int pageIndex,
                                                               @RequestParam(defaultValue = "10") int pageSize){
        return kitchenService.getOrdersByStatus(status, pageIndex, pageSize);
    }

    @PostMapping("/item")
    public ResponseEntity<String> postNewRestaurantMenuItem(@RequestBody RestaurantMenuItemDto request) {
        return kitchenService.postNewRestaurantMenuItem(request);
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<String> deleteRestaurantMenuItemById(@PathVariable("id") Long id){
        return kitchenService.deleteRestaurantMenuItemById(id);
    }

    @PostMapping("/item/{id}")
    public ResponseEntity<String> changePriceInMenuItem(@PathVariable("id") Long id,
                                                        @RequestBody PriceDto request) {
        return kitchenService.changePriceInMenuItemById(id, request);
    }

}
