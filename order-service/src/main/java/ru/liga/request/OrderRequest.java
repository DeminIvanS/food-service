package ru.liga.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.liga.dto.MenuItemDto;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @Schema(description = "ID restaurant")
    private UUID restaurantId;

    @Schema(description = "Item list in menu")
    private List<MenuItemDto> menuItems;
}
