package ru.liga.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.dto.CustomerDto;
import ru.liga.dto.DeliveryDto;
import ru.liga.dto.OrderChangeDto;
import ru.liga.dto.RestaurantDto;
import ru.liga.handler.EntityException;
import ru.liga.handler.StatusException;
import ru.liga.model.entities.Courier;
import ru.liga.model.entities.Customer;
import ru.liga.model.entities.Order;
import ru.liga.model.entities.Restaurant;
import ru.liga.model.statuses.CourierStatus;
import ru.liga.model.statuses.OrderStatus;
import ru.liga.rabbit.RabbitProducerServiceImpl;
import ru.liga.repository.CourierRepository;
import ru.liga.repository.CustomerRepository;
import ru.liga.repository.OrderRepository;
import ru.liga.repository.RestaurantRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DeliveryService {

    private final OrderRepository orderRepository;
    private final RabbitProducerServiceImpl rabbitProducerService;
    private final CourierRepository courierRepository;
    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public DeliveryService(OrderRepository orderRepository,
                           RabbitProducerServiceImpl rabbitProducerService,
                           CourierRepository courierRepository,
                           RestaurantRepository restaurantRepository,
                           CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.rabbitProducerService = rabbitProducerService;
        this.courierRepository = courierRepository;
        this.restaurantRepository = restaurantRepository;
        this.customerRepository = customerRepository;
    }

    private double calculateDistance(String courierCoordinates, String destinationCoordinates) {
        String[] parts1 = courierCoordinates.split(",");
        String[] parts2 = destinationCoordinates.split(",");

        if (parts1.length != 2 || parts2.length != 2) {
            throw new IllegalArgumentException("Неправильный формат координат");
        }

        double latitude1 = Double.parseDouble(parts1[0].trim());
        double longitude1 = Double.parseDouble(parts1[1].trim());
        double latitude2 = Double.parseDouble(parts2[0].trim());
        double longitude2 = Double.parseDouble(parts2[1].trim());

        double result = calculateCoordinates(latitude1, longitude1, latitude2, longitude2);

        return Math.round(result * 10.0) / 10.0;
    }

    private double calculateCoordinates(double latitude1, double longitude1,
                                           double latitude2, double longitude2) {
        double earthRadius = 6371;

        double dLatitude = Math.toRadians(latitude2 - latitude1);
        double dLongitude = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2) +
                Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) *
                        Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }


    private DeliveryDto toDeliveryDto(Order order) {

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurant().getId())
                .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_NOT_FOUND));

        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new EntityException(StatusException.CUSTOMER_NOT_FOUND));


        Courier courier = courierRepository.findById(5L)
                .orElseThrow(() -> new EntityException(StatusException.CUSTOMER_NOT_FOUND));

        String restaurantCoordinates = restaurant.getAddress();
        String customerCoordinates = customer.getAddress();
        String courierCoordinates = courier.getCoordinates();
        Double distanceFromCourierToRestaurant = calculateDistance(restaurantCoordinates, courierCoordinates);
        Double distanceFromRestaurantToCustomer = calculateDistance(customerCoordinates, restaurantCoordinates);

        RestaurantDto restaurantDto = new RestaurantDto()
                .setAddress(restaurant.getAddress())
                .setDistance(distanceFromCourierToRestaurant);

        CustomerDto customerDto = new CustomerDto()
                .setAddress(customer.getAddress())
                .setDistance(distanceFromRestaurantToCustomer);

        return new DeliveryDto()
                .setOrderId(order.getId())
                .setPayment("payment")
                .setRestaurant(restaurantDto)
                .setCustomer(customerDto);
    }

    public ResponseEntity<Map<String, Object>> getDeliveriesByStatus(String status, int pageIndex, int pageSize) {

        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        Page<Order> orderPage = orderRepository
                .findOrderByStatus(OrderStatus.valueOf(status.toUpperCase()), pageRequest);

        if (orderPage.isEmpty())
            throw new EntityException(StatusException.ORDER_NOT_FOUND);

        List<Order> orders = orderPage.getContent();
        List<DeliveryDto> deliveryDtos = orders.stream()
                .map(this::toDeliveryDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("orders", deliveryDtos);
        response.put("page_index", pageIndex);
        response.put("page_count", pageSize);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> setDeliveryStatusByOrderId(Long orderId, OrderChangeDto orderChangeDto) {

        Order orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityException(StatusException.ORDER_NOT_FOUND));

        String orderAction = orderChangeDto.getOrderChange().toUpperCase();

        if (OrderStatus.DELIVERY_PICKING.toString().equals(orderAction)) {
            rabbitProducerService.sendMessage("<courier_name> will pick that order!", "courier.response");
            orderEntity.setCourier(courierRepository.findCourierById(1L));
        } else if (OrderStatus.DELIVERY_DENIED.toString().equals(orderAction)) {
            rabbitProducerService.sendMessage("Delivery denied", "courier.response");
        }

        rabbitProducerService.sendMessage("New status: " + orderAction, "delivery.status.update");

        orderEntity.setStatus(OrderStatus.valueOf(orderAction));
        orderRepository.save(orderEntity);

        return ResponseEntity.ok().build();
    }

    public void processNewDelivery(Order order) throws InterruptedException {

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurant().getId())
                .orElseThrow(() -> new EntityException(StatusException.RESTAURANT_NOT_FOUND));
        String restaurantCoordinates = restaurant.getAddress();

        List<Courier> waitingCouriers = courierRepository.findAllByStatus(CourierStatus.PENDING);

        if (waitingCouriers.isEmpty()) {
            wait(120000);
            processNewDelivery(order);

        }

        Map<Double, Courier> courierDistances = new HashMap<>();

        for (Courier courier : waitingCouriers) {
            String courierCoordinates = courier.getCoordinates();
            double courierDistanceToRestaurant = calculateDistance(restaurantCoordinates, courierCoordinates);
            courierDistances.put(courierDistanceToRestaurant, courier);
        }

        sendMessageToNearbyCourier(courierDistances, order);
    }

    private void sendMessageToNearbyCourier(Map<Double, Courier> nearbyCouriers, Order order) {

        Courier nearbyCourier = nearbyCouriers.remove(Collections.min(nearbyCouriers.keySet()));
        if (nearbyCourier.getStatus().equals(CourierStatus.PICKING)) {
            order.setCourier(nearbyCourier);
        } else {
            sendMessageToNearbyCourier(nearbyCouriers, order);
        }
    }
}
