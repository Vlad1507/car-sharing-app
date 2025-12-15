package com.app.carsharing.dto.payment;

import java.math.BigDecimal;

public record PaymentDto(
        Long id,
        String paymentStatus,
        String paymentType,
        Long rentalId,
        String sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {
}
