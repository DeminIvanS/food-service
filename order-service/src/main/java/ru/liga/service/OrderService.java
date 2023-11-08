package ru.liga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.dto.MenuItemDto;
import ru.liga.dto.MessageStatusUpdate;
import ru.liga.dto.OrderDto;
import ru.liga.dto.OrderMessage;
import ru.liga.entity.*;
import ru.liga.handler.EntityException;
import ru.liga.handler.ExceptionStatus;
import ru.liga.rabbit.service.RabbitMQProducerServiceImpl;
import ru.liga.repository.*;
import ru.liga.request.OrderRequest;
import ru.liga.response.ResponseDto;
import ru.liga.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ComponentScan
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepo orderItemRepository;
    private final RestaurantRepo restaurantRepository;
    private final RestaurantMenuItemRepo restaurantMenuItemRepository;
    private final CustomerRepo customerRepository;
    private final RabbitMQProducerServiceImpl rabbitMQProducerService;
    private final ObjectMapper objectMapper;

    private String tryToSerializeOrderEntityAsString(Order order) {
        Map<UUID, Long> orderItemsIdAndQuantity = new HashMap<>();
        order.getItems()
                .forEach(item -> orderItemsIdAndQuantity.put(item.getId(), item.getQuantity()));

        OrderMessage message = new OrderMessage()
                .setId(order.getId())

                .setCustomerCoordinates(order.getCustomer().getAddress())
                .setRestaurantCoordinates(order.getRestaurant().getAddress())
                .setOrderItemsIdAndQuantity(orderItemsIdAndQuantity);

        String orderInLine;
        try {
            orderInLine = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return orderInLine;
    }

    private OrderItem mapOrderItem(Order order, UUID menuItemId, Long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("The quantity must be greater than zero");
        Restaurant restaurant = order.getRestaurant();

        List<RestaurantMenuItem> menu = restaurant.getRestaurantMenuItems();

        RestaurantMenuItem restaurantMenuItemEntity = restaurantMenuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new EntityException(ExceptionStatus.MENU_ITEM_NOT_FOUND));

        if (!menu.contains(restaurantMenuItemEntity)) {
            log.warn("Restaurant menu item not found " + restaurant.getName()
                    + ". Items for order: " + menu);
            throw new EntityException(ExceptionStatus.MENU_ITEM_NOT_FOUND);
        }

        return new OrderItem()
                .setOrderId(order.getId())
                .setQuantity(quantity)
                .setRestaurantMenuItem(restaurantMenuItemEntity)
                .setPrice(restaurantMenuItemEntity.getPrice() * quantity);
    }

    private Order mapOrderEntity(Customer customer, Restaurant restaurant) {

        return new Order()
                .setStatus(OrderStatus.CUSTOMER_CREATED)
                .setCustomer(customer)
                .setRestaurant(restaurant)
                .setLocalDateTime(LocalDateTime.now());
    }

    public ResponseEntity<ResponseDto<OrderDto>> getOrders(int pageIndex, int pageSize) {

        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        Page<Order> orderPage = orderRepository.findAll(pageRequest);
        List<Order> orders = orderPage.getContent();

        if (orders.isEmpty())
            throw new EntityException(ExceptionStatus.ORDER_NOT_FOUND);

        List<OrderDto> orderDTOS = new ArrayList<>();
        for (Order o : orders) {
            String restaurant = o.getRestaurant().getName();
            OrderDto dto = OrderDto.toOrderDto(o, restaurant);
            orderDTOS.add(dto);
        }

        ResponseDto<OrderDto> response = new ResponseDto<OrderDto>()
                .setOrders(orderDTOS)
                .setPageIndex(pageIndex)
                .setPageCount(pageSize);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<OrderDto> getOrderById(UUID id) {

        Order orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        String restaurantName = orderEntity.getRestaurant().getName();

        return ResponseEntity.ok().body(OrderDto.toOrderDto(orderEntity, restaurantName));
    }

    @Transactional
    public ResponseEntity<String> postNewOrder(OrderRequest orderRequest) {

        UUID uuid = UUID.randomUUID();

        UUID restaurantId = orderRequest.getRestaurantId();
        log.info("searched by id = " + restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityException(ExceptionStatus.RESTAURANT_NOT_FOUND));

        log.info("searched by UUID = 3");

        Customer customer = customerRepository.findById(new UUID(3L,3L))
                .orElseThrow(() -> new EntityException(ExceptionStatus.CUSTOMER_NOT_FOUND));
        Order orderEntity = mapOrderEntity(customer, restaurant);

        log.info("saved into DB...");
        Order savedOrder = orderRepository.save(orderEntity);
        log.info("Order " + savedOrder + " saved!");

        List<MenuItemDto> menuItemDTOS = orderRequest.getMenuItems();

        log.info("Mapping DTO item order: " + menuItemDTOS );
        List<OrderItem> orderItems = menuItemDTOS.stream()
                .map(orderItem -> mapOrderItem(savedOrder, orderItem.getMenuItemId(), orderItem.getQuantity()))
                .collect(Collectors.toList());

        log.info("Mapping successfully! Saved in DB...");
        orderItemRepository.saveAll(orderItems);
        log.info("item order: " + orderItems + " saved!");

        return ResponseEntity.status(HttpStatus.CREATED).body("Order created, waiting pay");
    }

    public ResponseEntity<String> imitatePayment(UUID id, Boolean isPaid) {

        if (isPaid == null) throw new IllegalArgumentException();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        OrderStatus currentOrderStatus = order.getStatus();
        if (isPaid && (currentOrderStatus == OrderStatus.CUSTOMER_CREATED
                || currentOrderStatus == OrderStatus.CUSTOMER_CANCELLED)) {
            order.setStatus(OrderStatus.CUSTOMER_PAID);
            rabbitMQProducerService.sendMessage(tryToSerializeOrderEntityAsString(order),
                    "new.order");
        } else {
            order.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        }

        orderRepository.save(order);
        return ResponseEntity.ok("Status order: " + currentOrderStatus);
    }

    public String updateOrderStatusById(UUID id, String newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.valueOf(newStatus.toUpperCase()));
        orderRepository.save(order);

        return "Status order id=" + id + " changed: " + newStatus;
    }

    public void processStatusUpdate(String message) {
        MessageStatusUpdate updateMessage;
        try {
            updateMessage = objectMapper.readValue(message, MessageStatusUpdate.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        updateOrderStatusById(updateMessage.getOrderId(), updateMessage.getNewStatus());
    }
}
