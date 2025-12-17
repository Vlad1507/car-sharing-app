package com.app.carsharing.service.payment;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentDto createPayment(CreatePaymentRequestSessionDto requestSessionDto);

    Page<PaymentDto> findPaymentsByUserId(Long userId, Pageable pageable, String username);

    PaymentDto getSuccessfulPayment(String sessionId);

    PaymentDto getCanceledPayment(String sessionId);
}
