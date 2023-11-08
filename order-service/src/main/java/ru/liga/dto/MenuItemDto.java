package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class MenuItemDto {
    @Schema(description = "quantity")
    private Long quantity;

    @Schema(description = "ID item in menu")
    private UUID menuItemId;
}
