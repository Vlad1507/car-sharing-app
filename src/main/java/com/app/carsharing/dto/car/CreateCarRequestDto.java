package com.app.carsharing.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String bodyType,
        @NotNull
        @Positive
        BigDecimal dailyFee,
        @NotNull
        @Positive
        Integer quantity
) {
}
