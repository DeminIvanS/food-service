package ru.liga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import ru.liga.dto.MessageStatusUpdate;
import ru.liga.dto.OrderMessage;
import ru.liga.handler.EntityException;
import ru.liga.handler.ExceptionStatus;
import ru.liga.model.entity.Courier;
import ru.liga.model.status.CourierStatus;
import ru.liga.rabbit.service.RabbitMQProducerServiceImpl;
import ru.liga.repo.CourierRepo;

import java.util.*;


@Service
@RequiredArgsConstructor
@ComponentScan
@Slf4j
public class DeliveryService {

    private final CourierRepo courierRepository;
    private final ObjectMapper objectMapper;
    private final RabbitMQProducerServiceImpl rabbitMQProducerService;
    private final Map<UUID, OrderMessage> orders = new HashMap<UUID, OrderMessage>();

    private double calculateDistance(String courierCoordinates, String destinationCoordinates) {
        String[] parts1 = courierCoordinates.split(",");
        String[] parts2 = destinationCoordinates.split(",");

        if (parts1.length != 2 || parts2.length != 2) {
            throw new IllegalArgumentException("Incorrect coordinate format\n" +
                    "Correct format: '12.3456789, 12.3465789' ");
        }

        double latitude1 = Double.parseDouble(parts1[0].trim());
        double longitude1 = Double.parseDouble(parts1[1].trim());
        double latitude2 = Double.parseDouble(parts2[0].trim());
        double longitude2 = Double.parseDouble(parts2[1].trim());

        double result = calculateMathematically(latitude1, longitude1, latitude2, longitude2);

        return Math.round(result * 10.0) / 10.0;
    }

    private double calculateMathematically(double latitude1, double longitude1,
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

    private String tryToSerializeStatusUpdateAsString(UUID orderId, UUID courierId, String newStatus) {
        MessageStatusUpdate update = new MessageStatusUpdate()
                .setNewStatus(newStatus)
                .setOrderId(orderId)
                .setCourierId(courierId);
        String message;
        try {
            message = objectMapper.writeValueAsString(update);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public Collection<OrderMessage> getAvailableDeliveries() {
        return orders.values();
    }

    public String setDeliveryStatusByOrderId(UUID orderId, String newStatus) {
        String message = tryToSerializeStatusUpdateAsString(orderId, null, newStatus);
        rabbitMQProducerService.sendMessage(message, "delivery.status.update");

        if ("delivery_picking".equals(newStatus)) {
            Courier courier = courierRepository.findById(1L)
                    .orElseThrow(() -> new EntityException(ExceptionStatus.COURIER_NOT_FOUND));
            rabbitMQProducerService.sendMessage(tryToSerializeStatusUpdateAsString(orderId, courier.getId(), newStatus),
                    "delivery.status.update");
        }
        orders.remove(orderId);
        return "Статус заказа id=" + orderId + " изменён на: " + newStatus;
    }

    public void processNewDelivery(String message) {
        OrderMessage order;
        try {
            order = objectMapper.readValue(message, OrderMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        orders.put(order.getId(), order);

        String restaurantCoordinates = order.getRestaurantCoordinates();
        List<Courier> waitingCouriers = courierRepository.findAllByStatus(CourierStatus.PENDING);
        Map<Double, Courier> courierDistances = new HashMap<>();

        for (Courier courier : waitingCouriers) {
            String courierCoordinates = courier.getCoordinates();
            double courierDistanceToRestaurant = calculateDistance(restaurantCoordinates, courierCoordinates);
            courierDistances.put(courierDistanceToRestaurant, courier);
        }

        sendMessageToNearestCourier(courierDistances, order);
    }

    private void sendMessageToNearestCourier(Map<Double, Courier> nearestCouriers, OrderMessage order) {

        Courier nearbyCourier = nearestCouriers.remove(Collections.min(nearestCouriers.keySet()));
        log.info("New order to delivery: " + order);

    }


}
