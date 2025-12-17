package com.app.carsharing.dto.car;

import java.math.BigDecimal;

public record CarDto(
        Long id,
        String model,
        String bodyType,
        int availableCars,
        BigDecimal dailyFee
) {
}
