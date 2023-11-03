package ru.liga.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeliveryDto {

    private Long orderId;
    private RestaurantDto restaurant;
    private CustomerDto customer;
    private String payment;
}