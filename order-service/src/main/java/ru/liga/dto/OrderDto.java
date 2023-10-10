package ru.liga.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "order DTO")
@Data
@Accessors(chain = true)
public class OrderDto {
    private Long id;
    private Double summ;
}
