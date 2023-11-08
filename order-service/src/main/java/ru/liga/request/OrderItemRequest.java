package ru.liga.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class OrderItemRequest {

    @Schema(description = "ID item in menu")
    private UUID restaurantMenuItemId;

    @Schema(description = "quantity")
    private Long quantity;
}