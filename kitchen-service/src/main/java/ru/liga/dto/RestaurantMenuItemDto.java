package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestaurantMenuItemDto {
    private String name;
    private Double price;
    private String imageUrl;
    private String description;
}
