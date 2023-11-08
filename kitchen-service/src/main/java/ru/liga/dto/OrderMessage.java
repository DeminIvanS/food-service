package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class OrderMessage {

    private UUID id;
    private String customerCoordinates;
    private String restaurantCoordinates;
    private Map<Long, Long> orderItemsIdAndQuantity;
}
