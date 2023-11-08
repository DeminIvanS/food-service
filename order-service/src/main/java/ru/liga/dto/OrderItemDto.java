package ru.liga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.liga.entity.OrderItem;

@Data
@Accessors(chain = true)
public class OrderItemDto {
    @Schema(description = "Price")
    private Double price;

    @Schema(description = "Quantity")
    private Long quantity;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Image")
    private String imageUrl;

    public static OrderItemDto toOrderItemDto(OrderItem item) {

        return new OrderItemDto()
                .setImageUrl(item.getRestaurantMenuItem().getImage())
                .setPrice(item.getPrice())
                .setQuantity(item.getQuantity())
                .setDescription(item.getRestaurantMenuItem().getDescription());
    }
}
