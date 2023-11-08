package ru.liga.status;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public enum KitchenStatus {
    @Enumerated(EnumType.STRING)
    OPEN,
    @Enumerated(EnumType.STRING)
    CLOSED
}
