package ru.liga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestaurantDto {

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Status")
    private KitchenStatus status;

    @Schema(description = "Order list")
    private List<Order> orders;

    @Schema(description = "Item list in menu")
    private List<RestaurantMenuItem> menuItems;

    public static RestaurantDto mapRestaurantEntityToDTO(Restaurant restaurant) {
        return new RestaurantDto()
                .setAddress(restaurant.getAddress())
                .setStatus(restaurant.getStatus())
                .setOrders(restaurant.getOrders())
                .setMenuItems(restaurant.getRestaurantMenuItems());
    }
}
}
