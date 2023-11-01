package ru.liga.request;

import lombok.Data;
import ru.liga.dto.MenuItemDto;

import java.util.List;

@Data
public class OrderReq {

    private Long restaurantId;
    private List<MenuItemDto> menuItemDtoList;
}
