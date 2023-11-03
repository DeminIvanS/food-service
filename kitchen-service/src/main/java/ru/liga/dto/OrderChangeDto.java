package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderChangeDto {

    private String orderChange;
}
