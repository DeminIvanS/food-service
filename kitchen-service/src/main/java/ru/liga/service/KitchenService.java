package ru.liga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.dto.OrderDto;
import ru.liga.dto.OrderItemDto;
import ru.liga.dto.PriceDto;
import ru.liga.dto.RestaurantMenuItemDto;
import ru.liga.handler.EntityException;
import ru.liga.handler.StatusException;
import ru.liga.model.entities.Order;
import ru.liga.model.entities.OrderItem;
import ru.liga.model.entities.Restaurant;
import ru.liga.model.entities.RestaurantMenuItem;
import ru.liga.model.enums.OrderStatus;
import ru.liga.repository.OrderItemRepository;
import ru.liga.repository.OrderRepository;
import ru.liga.repository.RestaurantMenuItemRepository;
import ru.liga.repository.RestaurantRepository;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class KitchenService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMenuItemRepository restaurantMenuItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public KitchenService(RestaurantRepository restaurantRepository,
                          RestaurantMenuItemRepository restaurantMenuItemRepository,
                          OrderItemRepository orderItemRepository,
                          OrderRepository orderRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantMenuItemRepository = restaurantMenuItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    private List<OrderItemDto> toOrderItemDto(List<OrderItem> orderItemEntities) {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (OrderItem i : orderItemEntities) {
            OrderItemDto dto = new OrderItemDto()
                    .setMenuItemId(i.getRestaurantMenuItem().getId())
                    .setQuantity(i.getQuantity());
            orderItemDtos.add(dto);
        }
        return orderItemDtos;
    }
    private List<OrderDto> toOrderDto(List<Order> orders) {
        List<OrderDto> orderDTOS = new ArrayList<>();
        for (Order o : orders) {
            List<OrderItem> items = o.getItemList();

            OrderDto dto = new OrderDto()
                    .setId(o.getId())
                    .setOrderItemDto(toOrderItemDto(items));

            orderDTOS.add(dto);
        }
        return orderDTOS;
    }

    private RestaurantMenuItem mapRestaurantMenuItem(RestaurantMenuItemDto request) {
       Restaurant restaurant = restaurantRepository.findById(4L)
             .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_NOT_FOUND));

        return new RestaurantMenuItem()
                .setRestaurant(restaurant)
                .setName(request.getName())
                .setPrice(request.getPrice())
                .setImageUrl(request.getImageUrl())
                .setDescription(request.getDescription());
    }

    public ResponseEntity<Map<String, Object>> getOrdersByStatus(String status, int index, int size) {
        PageRequest pageRequest = PageRequest.of(index, size);
        Page<Order> order = orderRepository.findOrderByStatus(OrderStatus.valueOf(status.toUpperCase()),  pageRequest);


        if (order.isEmpty())
            throw new EntityException(StatusException.ORDER_NOT_FOUND);

        List<Order> orders = order.getContent();
        List<OrderDto> orderDTOS = toOrderDto(orders);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOS);
        response.put("page_index", index);
        response.put("page_count", size);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> postNewRestaurantMenuItem(RestaurantMenuItemDto request) {

        RestaurantMenuItem savedItem = restaurantMenuItemRepository.save(mapRestaurantMenuItem(request));
        savedItem.getRestaurant().addItemMenu(savedItem);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<String> deleteRestaurantMenuItemById(Long id) {

        try {
            List<OrderItem> orderItemEntities = orderItemRepository.findAllById(id)
                    .orElseThrow(() -> new EntityException(StatusException.ORDER_ITEM_NOT_FOUND));
            orderItemRepository.deleteAll(orderItemEntities);
        } catch (EntityException ignored) {}

        restaurantMenuItemRepository.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public ResponseEntity<String> changePriceInMenuItemById(Long id, PriceDto request) {

        Double newPrice = request.getNewPrice();
        if (newPrice <= 0) throw new IllegalArgumentException();

        RestaurantMenuItem menuItem = restaurantMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_MENU_ITEM_NOT_FOUND));

        menuItem.setPrice(newPrice);
        restaurantMenuItemRepository.save(menuItem);

        return ResponseEntity.ok().build();
    }


}
