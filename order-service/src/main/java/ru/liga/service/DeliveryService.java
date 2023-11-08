package ru.liga.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import ru.liga.dto.DeliveryConfirmationMessage;
import ru.liga.dto.MessageStatusUpdate;
import ru.liga.dto.OrderMessage;
import ru.liga.handler.EntityException;
import ru.liga.handler.ExceptionStatus;
import ru.liga.model.entity.Courier;
import ru.liga.model.status.CourierStatus;
import ru.liga.rabbit.service.RabbitMQProducerServiceImpl;
import ru.liga.repository.CourierRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
@ComponentScan
@Slf4j
public class DeliveryService {

    private final CourierRepo courierRepo;
    private final ObjectMapper objectMapper;
    private final RabbitMQProducerServiceImpl rabbitMQProducerService;
    private final Map<UUID, OrderMessage> orders = new HashMap<>();

    private double calculateDistance(String courierCoordinates, String destinationCoordinates) {
        String[] parts1 = courierCoordinates.split(",");
        String[] parts2 = destinationCoordinates.split(",");

        if (parts1.length != 2 || parts2.length != 2) {
            throw new IllegalArgumentException("Incorrect coordinate format\n" +
                    "Correct coordinate format: '12.34567890, 12.34567890' ");
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

    private String tryToSerializeStatusUpdateAsString(UUID orderId, String newStatus) {
        MessageStatusUpdate update = new MessageStatusUpdate().setNewStatus(newStatus).setOrderId(orderId);
        String message;
        try {
            message = objectMapper.writeValueAsString(update);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private String tryToSerializeDeliveryPickingAsString(UUID orderId, UUID courierId) {
        DeliveryConfirmationMessage confirmationMessage = new DeliveryConfirmationMessage().setOrderId(orderId).setCourierId(courierId);
        String message;
        try {
            message = objectMapper.writeValueAsString(confirmationMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public Collection<OrderMessage> getAvailableDeliveries() {
        return orders.values();
    }

    public String setDeliveryStatusByOrderId(UUID orderId, String newStatus) {
        String message = tryToSerializeStatusUpdateAsString(orderId, newStatus);
        rabbitMQProducerService.sendMessage(message, "delivery.status.update");

        if ("delivery_picking".equals(newStatus)) {
            Courier courier = courierRepo.findById(1L)
                    .orElseThrow(() -> new EntityException(ExceptionStatus.COURIER_NOT_FOUND));
            rabbitMQProducerService.sendMessage(tryToSerializeDeliveryPickingAsString(orderId, courier.getId()),
                    "courier.appointment");
        }
        orders.remove(orderId);
        return "Order status id=" + orderId + " changed: " + newStatus;
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
        List<Courier> waitingCouriers = courierRepo.findAllByStatus(CourierStatus.PENDING);
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
        log.info("New Delivery: " + order );

    }


}
