package com.app.carsharing.dto.car;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateCarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String bodyType,
        @NotNull
        @Positive
        @Min(0)
        Integer quantityToAdd,
        @NotNull
        @Positive
        BigDecimal dailyFee
) {
}
