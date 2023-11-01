package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderItemDto {

    private Double price;
    private Double quantity;
    private String description;
    private String imageUrl;

}
