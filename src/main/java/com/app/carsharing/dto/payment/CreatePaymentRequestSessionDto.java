package com.app.carsharing.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestSessionDto(
        @NotBlank String paymentType,
        @NotNull Long rentalId
) {
}
