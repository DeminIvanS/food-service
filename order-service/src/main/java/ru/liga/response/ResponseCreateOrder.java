package ru.liga.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResponseCreateOrder {
    private Long id;
    private String secretPaymentUrl;
    private String estimatedDeliveryTime;
}
