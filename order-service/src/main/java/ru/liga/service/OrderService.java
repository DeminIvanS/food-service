package ru.liga.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.dto.MenuItemDto;
import ru.liga.dto.OrderDto;
import ru.liga.dto.OrderItemDto;
import ru.liga.dto.RestaurantDto;
import ru.liga.handler.EntityException;
import ru.liga.handler.StatusException;
import ru.liga.mapper.RestaurantMapper;
import ru.liga.model.entities.*;
import ru.liga.model.statuses.OrderStatus;
import ru.liga.repository.*;
import ru.liga.request.OrderItemReq;
import ru.liga.request.OrderReq;
import ru.liga.response.ResponseCreateOrder;


import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final RestaurantMenuItemRepository restaurantMenuItemRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        RestaurantRepository restaurantRepository,
                        RestaurantMapper restaurantMapper,
                        RestaurantMenuItemRepository restaurantMenuItemRepository,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
        this.restaurantMenuItemRepository = restaurantMenuItemRepository;
        this.customerRepository = customerRepository;
    }
    private OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto()
                .setDescription(orderItem.getRestaurantMenuItem().getDescription())
                .setImageUrl(orderItem.getRestaurantMenuItem().getImageUrl())
                .setPrice(orderItem.getPrice())
                .setQuantity(orderItem.getQuantity());
    }
    private List<OrderItemDto> toOrderItemDto(List<OrderItem> items) {

        return items.stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());
    }
    private OrderDto toOrderDto(Order order){
        return new OrderDto()
                .setId(order.getId())
                .setRestaurant(new RestaurantDto().setAddress(order.getRestaurant().getAddress()))
                .setItems(toOrderItemDto(order.getItemList()))
                .setTimestamp(order.getTimestamp());
    }

    private List<OrderDto> toOrderDto(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderDto).collect(Collectors.toList());

    }
    public ResponseEntity<Map<String, Object>> getOrders(int pageIndex,int pageSize){
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        Page<Order> orderPage = orderRepository.findAll(pageRequest);

        if(orderPage.isEmpty())
            throw new EntityException(StatusException.ORDER_NOT_FOUND);
        List<Order> orders = orderPage.getContent();
        List<OrderDto> orderDto = toOrderDto(orders);
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDto);
        response.put("page_index", pageIndex);
        response.put("page_count", pageSize);
        return  ResponseEntity.ok(response);
    }
    public ResponseEntity<ResponseCreateOrder> createNewOrder(OrderReq orderReq) {
        Order order = new Order();
        Restaurant restaurant = restaurantRepository.findById(orderReq.getRestaurantId())
                .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_NOT_FOUND));

        Customer customer = customerRepository.findById(1L)
                .orElseThrow(()->new EntityException(StatusException.CUSTOMER_NOT_FOUND));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Order orderSaved = orderRepository.save(orderSetStatus(order,customer,restaurant,timestamp));
        for(MenuItemDto dto : orderReq.getMenuItemDtoList()) {
            RestaurantMenuItem menuItem = restaurantMenuItemRepository.findById(dto.getMenuItemId())
                    .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_MENU_ITEM_NOT_FOUND));

            Double quantity = dto.getQuantity();
            if(quantity<=0) throw new IllegalArgumentException();
            OrderItem item = new OrderItem();
            Double price = menuItem.getPrice();

            OrderItem itemSaved = orderItemRepository.save(orderList(item,order,menuItem,quantity,price));
            order.addOrderItem(itemSaved);

        }
        restaurant.addOrder(order);
        customer.addOrder(order);

        ResponseCreateOrder response = new ResponseCreateOrder().setId(orderSaved.getId())
                .setSecretPaymentUrl("url")
                .setEstimatedDeliveryTime("soon");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    private OrderItem orderList(OrderItem item, Order order, RestaurantMenuItem menuItem, Double quantity, Double price){
        item.setOrder(order)
                .setRestaurantMenuItem(menuItem)
                .setQuantity(quantity)
                .setPrice(price*quantity);
        return item;
    }
    private Order orderSetStatus(Order order, Customer customer, Restaurant restaurant, Timestamp timestamp){
        order.setStatus(OrderStatus.CUSTOMER_CREATED)
                .setCustomer(customer)
                .setRestaurant(restaurant)
                .setTimestamp(timestamp);
        return order;
    }
    public ResponseEntity<OrderDto> getOrderById(Long id) {

        Order orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityException(StatusException.ORDER_NOT_FOUND));

        return ResponseEntity.ok().body(toOrderDto(orderEntity));
    }
    public ResponseEntity<String> deleteOrderById(Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    public ResponseEntity<String> createNewOrderItem(Long orderId, OrderItemReq request) {

        Order orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityException(StatusException.ORDER_NOT_FOUND));

        Long restaurantMenuItemId = request.getRestaurantMenuItemId();
        Double quantity = request.getQuantity();
        if (restaurantMenuItemId <= 0 || quantity <= 0) throw new IllegalArgumentException();

        RestaurantMenuItem restaurantMenuItem = restaurantMenuItemRepository.findById(restaurantMenuItemId)
                .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_MENU_ITEM_NOT_FOUND));

        OrderItem newOrderItemEntity = new OrderItem()
                .setOrder(orderEntity)
                .setQuantity(quantity)
                .setRestaurantMenuItem(restaurantMenuItem)
                .setPrice(restaurantMenuItem.getPrice() * quantity);

        OrderItem savedOrderItemEntity = orderItemRepository.save(newOrderItemEntity);
        orderEntity.addOrderItem(savedOrderItemEntity);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    public ResponseEntity<String> deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public ResponseEntity<Map<String, Object>> getRestaurantByIdBatis(Long id) {
        if (id <= 0)
            throw new IllegalArgumentException();
        Restaurant restaurant = restaurantMapper.findRestaurantById(id);
        if (restaurant == null)
            throw new EntityException(StatusException.RESTAURANT_NOT_FOUND);
        Map<String, Object> response = new HashMap<>();
        response.put("restaurant", restaurant);
        return ResponseEntity.ok(response);
    }
    public ResponseEntity<Map<String, Object>> getRestaurantByAddressBatis(String address) {

        Restaurant restaurant = restaurantMapper.findRestaurantByAddress(address);
        if (restaurant == null)
            throw new EntityException(StatusException.RESTAURANT_NOT_FOUND);

        Map<String, Object> response = new HashMap<>();
        response.put("restaurant", restaurant);
        return ResponseEntity.ok(response);
    }


}
