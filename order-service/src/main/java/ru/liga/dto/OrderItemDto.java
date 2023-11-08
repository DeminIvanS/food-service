package ru.liga.dto;

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

    public static OrderItemDto convertOrderItemToOrderItemDto(OrderItem item) {

        return new OrderItemDto()
                .setImage(item.getRestaurantMenuItem().getImage())
                .setPrice(item.getPrice())
                .setQuantity(item.getQuantity())
                .setDescription(item.getRestaurantMenuItem().getDescription());
    }
}
