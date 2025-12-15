package com.app.carsharing.util;

import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.model.Payment;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class PaymentUtil {

    public static PaymentDto getFinePaymentDtoForRental() {
        return new PaymentDto(
                1L,
                "PENDING",
                "FINE",
                2L,
                "https://checkout.stripe.com/pay/session_fine67890",
                "cs_test_session_fine67890",
                BigDecimal.valueOf(270)
        );
    }

    public static Payment getPendingPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentStatus(Payment.Status.PENDING);
        payment.setPaymentType(Payment.PaymentType.PAYMENT);
        payment.setRental(RentalUtil.getPendingRental());
        try {
            payment.setSessionUrl(new URI("https://checkout.stripe.com/pay/session_12345").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        payment.setSessionId("cs_test_session12345");
        payment.setAmountToPay(BigDecimal.valueOf(350));
        return payment;
    }

    public static Payment getCancelPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentStatus(Payment.Status.PENDING);
        payment.setPaymentType(Payment.PaymentType.PAYMENT);
        payment.setRental(RentalUtil.getCanceledRental());
        try {
            payment.setSessionUrl(
                    new URI("https://checkout.stripe.com/pay/cancel_session_12345").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        payment.setSessionId("cs_test_cancel_session12345");
        payment.setAmountToPay(BigDecimal.valueOf(350));
        return payment;
    }

    public static Payment getCanceledPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentStatus(Payment.Status.CANCELED);
        payment.setPaymentType(Payment.PaymentType.PAYMENT);
        payment.setRental(RentalUtil.getCanceledRental());
        try {
            payment.setSessionUrl(
                    new URI("https://checkout.stripe.com/pay/cancel_session_12345").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        payment.setSessionId("cs_test_cancel_session12345");
        payment.setAmountToPay(BigDecimal.valueOf(350));
        return payment;
    }

    public static PaymentDto getCancelledPaymentDto() {
        return new PaymentDto(
                4L,
                Payment.Status.CANCELED.name(),
                Payment.PaymentType.PAYMENT.name(),
                6L,
                getCancelPayment().getSessionUrl().toString(),
                getCancelPayment().getSessionId(),
                getCancelPayment().getAmountToPay()
        );
    }

    public static Payment getSuccessfulPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentStatus(Payment.Status.PAID);
        payment.setPaymentType(Payment.PaymentType.PAYMENT);
        payment.setRental(RentalUtil.getReturnedRental());
        try {
            payment.setSessionUrl(new URI("https://checkout.stripe.com/pay/session_12345").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        payment.setSessionId("cs_test_session12345");
        payment.setAmountToPay(BigDecimal.valueOf(350));
        return payment;
    }

    public static PaymentDto getPendingPaymentDto() {
        return new PaymentDto(
                getPendingPayment().getId(),
                getPendingPayment().getPaymentStatus().name(),
                getPendingPayment().getPaymentType().name(),
                RentalUtil.getPendingRental().getId(),
                getPendingPayment().getSessionUrl().toString(),
                getPendingPayment().getSessionId(),
                BigDecimal.valueOf(350)
        );
    }

    public static PaymentDto getSuccessfulRegularPaymentDto() {
        return new PaymentDto(
                getSuccessfulPayment().getId(),
                getSuccessfulPayment().getPaymentStatus().name(),
                getSuccessfulPayment().getPaymentType().name(),
                RentalUtil.getPendingRental().getId(),
                getSuccessfulPayment().getSessionUrl().toString(),
                getSuccessfulPayment().getSessionId(),
                getSuccessfulPayment().getAmountToPay()
        );
    }

    public static Payment getPendingFinePayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentStatus(Payment.Status.PENDING);
        payment.setPaymentType(Payment.PaymentType.FINE);
        payment.setRental(RentalUtil.getCompletedPendingFineRental());
        try {
            payment.setSessionUrl(new URI("https://checkout.stripe.com/pay/session_fine67890").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        payment.setSessionId("cs_test_session_fine67890");
        payment.setAmountToPay(BigDecimal.valueOf(210));
        return payment;
    }

    public static PaymentDto getFinePaymentDto() {
        return new PaymentDto(
                1L,
                "PENDING",
                "FINE",
                2L,
                "https://checkout.stripe.com/pay/session_fine67890",
                "cs_test_session_fine67890",
                BigDecimal.valueOf(210)
        );
    }

    public static PaymentDto getCompletedFinePaymentDto() {
        return new PaymentDto(
                1L,
                "PAID",
                "FINE",
                2L,
                "https://checkout.stripe.com/pay/session_fine67890",
                "cs_test_session_fine67890",
                BigDecimal.valueOf(210)
        );
    }
}
