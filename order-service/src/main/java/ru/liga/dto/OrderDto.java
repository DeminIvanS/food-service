package ru.liga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.liga.entity.Order;
import ru.liga.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class OrderDto {

    @Schema(description = "Order ID")
    private UUID id;

    private LocalDateTime localDateTime;

    @Schema(description = "ItemList in order")
    private List<OrderItemDto> items;

    public static OrderDto toOrderDto(Order order, UUID id) { //uuid restaurant

        List<OrderItem> items =  order.getItems();
        List<OrderItemDto> itemDTOS = items.stream()
                .map(OrderItemDto::toOrderItemDto)
                .collect(Collectors.toList());

        return new OrderDto()
                .setId(order.getId())
                .setItems(itemDTOS)
                .setTimestamp(order.getLocalDateTime());
    }
}

