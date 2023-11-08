package ru.liga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.dto.MenuItemDto;
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

import java.security.Timestamp;
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
        Map<Long, Long> orderItemsIdAndQuantity = new HashMap<>();
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
                .orElseThrow(() -> new EntityException(ExceptionStatus.RESTAURANT_MENU_ITEM_NOT_FOUND));

        if (!menu.contains(restaurantMenuItemEntity)) {
            log.warn("Указанные позиции отсутствуют в меню ресторана " + restaurant.getName()
                    + ". Доступные позиции для заказа: " + menu);
            throw new EntityException(ExceptionStatus.RESTAURANT_MENU_ITEM_NOT_FOUND);
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
                .setTimestamp(new Timestamp(System.currentTimeMillis()));
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

    public ResponseEntity<OrderDto> getOrderById(Long id) {

        Order orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        String restaurantName = orderEntity.getRestaurant().getName();

        return ResponseEntity.ok().body(OrderDto.toOrderDto(orderEntity, restaurantName));
    }

    @Transactional
    public ResponseEntity<String> postNewOrder(OrderRequest orderRequest) {

        UUID uuid = UUID.randomUUID();

        UUID restaurantId = orderRequest.getRestaurantId();
        log.info("Поиск ресторана по id = " + restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityException(ExceptionStatus.RESTAURANT_NOT_FOUND));

        log.info("Поиск заказчика по id = 5");
        Customer customer = customerRepository.findById(5L)
                .orElseThrow(() -> new EntityException(ExceptionStatus.CUSTOMER_NOT_FOUND));
        Order orderEntity = mapOrderEntity(customer, restaurant);

        log.info("Сохранение заказа в БД...");
        Order savedOrder = orderRepository.save(orderEntity);
        log.info("Заказ " + savedOrder + " сохранён!");

        List<MenuItemDto> menuItemDTOS = orderRequest.getMenuItems();

        log.info("Маппинг ДТО позиций заказа: " + menuItemDTOS + " на сущность...");
        List<OrderItem> orderItems = menuItemDTOS.stream()
                .map(orderItem -> mapOrderItem(savedOrder, orderItem.getMenuItemId(), orderItem.getQuantity()))
                .collect(Collectors.toList());

        log.info("Маппинг успешный! Сохранение в базу...");
        orderItemRepository.saveAll(orderItems);
        log.info("Позиции заказа: " + orderItems + " сохранены!");

        return ResponseEntity.status(HttpStatus.CREATED).body("Заказ создан, ожидаем оплату");
    }

    public ResponseEntity<String> imitatePayment(Long id, Boolean isPaid) {

        if (isPaid == null) throw new IllegalArgumentException();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        OrderStatus currentOrderStatus = order.getStatus();
        if (isPaid && (currentOrderStatus == OrderStatus.CUSTOMER_CREATED
                || currentOrderStatus == OrderStatus.CUSTOMER_CANCELLED)) {
            orderEntity.setStatus(OrderStatus.CUSTOMER_PAID);
            rabbitMQProducerService.sendMessage(tryToSerializeOrderEntityAsString(orderEntity),
                    "new.order");
        } else {
            orderEntity.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        }

        orderRepository.save(orderEntity);
        return ResponseEntity.ok("Статус заказа: " + currentOrderStatus);
    }

    public String updateOrderStatusById(Long id, String newStatus) {

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(ExceptionStatus.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.valueOf(newStatus.toUpperCase()));
        orderRepository.save(order);

        return "Статус заказа id=" + id + " изменён на: " + newStatus;
    }

    public void processStatusUpdate(String message) {
        StatusUpdateMessage updateMessage;
        try {
            updateMessage = objectMapper.readValue(message, StatusUpdateMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        updateOrderStatusById(updateMessage.getOrderId(), updateMessage.getNewStatus());
    }
}
