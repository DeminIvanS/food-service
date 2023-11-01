package ru.liga.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderItemReq {

    private Long restaurantMenuItemId;
    private Double quantity;
}
