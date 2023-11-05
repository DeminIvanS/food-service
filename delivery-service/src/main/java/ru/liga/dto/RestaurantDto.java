package ru.liga.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RestaurantDto {

    private String address;
    private Double distance;

}
