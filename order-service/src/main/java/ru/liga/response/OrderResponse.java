package ru.liga.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "SecretUrl for Pay")
    private String secretPaymentUrl;

    @Schema(description = "Time of arrival")
    private String estimatedTimeOfArrival;
}
