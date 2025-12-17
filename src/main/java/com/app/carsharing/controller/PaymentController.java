package com.app.carsharing.controller;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Receive all payments",
            description = "Returns payment transaction history")
    @GetMapping
    public Page<PaymentDto> getPayments(
            @RequestParam Long userId,
            Pageable pageable,
            Authentication authentication) {
        String username = authentication.getName();
        Page<PaymentDto> response = paymentService.findPaymentsByUserId(userId, pageable, username);
        return new ResponseEntity<>(response, HttpStatus.OK).getBody();
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @Operation(summary = "Create payment session",
            description = "Creates a session for payment or penalty transactions "
                    + "with redirection to Stripe")
    @PostMapping
    public ResponseEntity<PaymentDto> createPaymentSession(
            @RequestBody @Valid CreatePaymentRequestSessionDto requestSessionDto) {
        PaymentDto response = paymentService.createPayment(requestSessionDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Successful transaction",
            description = "Return payment information with session id")
    @GetMapping("/successful")
    @ResponseStatus(HttpStatus.TEMPORARY_REDIRECT)
    public ResponseEntity<PaymentDto> getSuccessfulPayments(
            @RequestParam(name = "session_id") String sessionId) {
        PaymentDto response = paymentService.getSuccessfulPayment(sessionId);
        return new ResponseEntity<>(response, HttpStatus.TEMPORARY_REDIRECT);
    }

    @Operation(summary = "Canceled transaction",
            description = "Return payment information about canceled transaction")
    @GetMapping("/canceled")
    @ResponseStatus(HttpStatus.TEMPORARY_REDIRECT)
    public ResponseEntity<PaymentDto> getCanceledPayments(
            @RequestParam(name = "session_id") String sessionId) {
        PaymentDto response = paymentService.getCanceledPayment(sessionId);
        return new ResponseEntity<>(response, HttpStatus.TEMPORARY_REDIRECT);
    }
}
