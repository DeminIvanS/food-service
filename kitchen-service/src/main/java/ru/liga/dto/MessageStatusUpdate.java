package ru.liga.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class MessageStatusUpdate {
    private UUID orderId;
    private String newStatus;
}
