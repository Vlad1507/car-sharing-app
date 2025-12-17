package com.app.carsharing.dto.car;

import java.math.BigDecimal;

public record CarListDto(
        Long id,
        String model,
        BigDecimal dailyFee
) {
}
