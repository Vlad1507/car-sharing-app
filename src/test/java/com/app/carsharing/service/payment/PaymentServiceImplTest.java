package com.app.carsharing.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.exception.PaymentStatusException;
import com.app.carsharing.exception.PaymentTypeException;
import com.app.carsharing.mapper.PaymentMapper;
import com.app.carsharing.model.Car;
import com.app.carsharing.model.Payment;
import com.app.carsharing.model.Rental;
import com.app.carsharing.model.User;
import com.app.carsharing.repository.car.CarRepository;
import com.app.carsharing.repository.payment.PaymentRepository;
import com.app.carsharing.repository.rental.RentalRepository;
import com.app.carsharing.repository.user.UserRepository;
import com.app.carsharing.service.notification.NotificationService;
import com.app.carsharing.service.stripe.StripeClient;
import com.app.carsharing.util.CarUtil;
import com.app.carsharing.util.PaymentUtil;
import com.app.carsharing.util.RentalUtil;
import com.app.carsharing.util.StripeUtil;
import com.app.carsharing.util.UserUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PaymentServiceImplTest {
    @MockitoBean
    private final RentalRepository rentalRepository;
    @MockitoBean
    private final PaymentRepository paymentRepository;
    @MockitoBean
    private final PaymentMapper paymentMapper;
    @MockitoBean
    private final StripeClient stripeClient;
    @MockitoBean
    private final CarRepository carRepository;
    @MockitoBean
    private final NotificationService notificationService;
    @MockitoBean
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Autowired
    PaymentServiceImplTest(
            RentalRepository rentalRepository,
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            StripeClient stripeClient,
            CarRepository carRepository,
            NotificationService notificationService,
            UserRepository userRepository,
            PaymentService paymentService) {
        this.rentalRepository = rentalRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.stripeClient = stripeClient;
        this.carRepository = carRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Test
    @DisplayName("Should create regular payment ")
    void createPayment_successfulPayment_shouldReturnPaymentDto() throws StripeException {
        Payment payment = PaymentUtil.getPendingPayment();
        Rental rental = RentalUtil.getPendingRental();
        CreatePaymentRequestSessionDto requestSessionDto = new CreatePaymentRequestSessionDto(
                Payment.PaymentType.PAYMENT.name(),
                rental.getId()
        );
        PaymentDto expected = PaymentUtil.getSuccessfulRegularPaymentDto();
        Session mockSession = StripeUtil.mockSession(payment.getSessionUrl().toString());

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(stripeClient
                .createSessionCheckout(anyLong(), anyString(),
                        anyString(), anyString(), anyString()))
                .thenReturn(mockSession);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentDto actual = paymentService.createPayment(requestSessionDto);
        assertEquals(expected, actual);
        assertEquals(expected.sessionUrl(), actual.sessionUrl());
        verify(rentalRepository, times(1)).findById(rental.getId());
        verify(stripeClient, times(1))
                .createSessionCheckout(any(), anyString(), anyString(), anyString(), anyString());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should create fine payment")
    void createPayment_successfulFine_shouldReturnRentalDto() throws StripeException {
        Payment payment = PaymentUtil.getPendingFinePayment();
        Rental rental = RentalUtil.getPendingFineRental();
        CreatePaymentRequestSessionDto requestSessionDto = new CreatePaymentRequestSessionDto(
                Payment.PaymentType.FINE.name(),
                rental.getId()
        );
        PaymentDto expected = PaymentUtil.getFinePaymentDto();
        Session mockSession = StripeUtil.mockSession(payment.getSessionUrl().toString());

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(stripeClient
                .createSessionCheckout(anyLong(), anyString(),
                        anyString(), anyString(), anyString()))
                .thenReturn(mockSession);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentDto actual = paymentService.createPayment(requestSessionDto);
        assertEquals(expected, actual);
        assertEquals(expected.sessionId(), actual.sessionId());
        verify(rentalRepository, times(1)).findById(rental.getId());
        verify(stripeClient, times(1))
                .createSessionCheckout(any(), anyString(), anyString(), anyString(), anyString());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw an error due to invalid or unsupported payment type")
    void createPayment_invalidPayment_shouldThrowError() {
        String unsupportedPaymentType = "DEBT";
        Rental rental = RentalUtil.getPendingFineRental();
        CreatePaymentRequestSessionDto requestSessionDto = new CreatePaymentRequestSessionDto(
                unsupportedPaymentType, rental.getId());
        String expectedMessage = "Unsupported or invalid payment type: " + unsupportedPaymentType;

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        PaymentTypeException actual = assertThrows(PaymentTypeException.class,
                () -> paymentService.createPayment(requestSessionDto));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should find payment by user id and return page of payment dto")
    void findPaymentsByUserId_validUserId_shouldReturnPaymentDto() {
        User user = UserUtil.getExistedUser();
        List<Rental> rentals = List.of(RentalUtil.getReturnedRental(),
                RentalUtil.getPendingRental());
        Payment pendingPayment = PaymentUtil.getPendingPayment();
        Payment successfulPayment = PaymentUtil.getSuccessfulPayment();
        List<Payment> paymentList = List.of(pendingPayment, successfulPayment);
        Page<Payment> payments = new PageImpl<>(paymentList);
        PaymentDto pendingPaymentDto = PaymentUtil.getPendingPaymentDto();
        PaymentDto successfulRegularPayment = PaymentUtil.getSuccessfulRegularPaymentDto();
        List<PaymentDto> paymentDtos = List.of(pendingPaymentDto, successfulRegularPayment);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserId(user.getId())).thenReturn(rentals);
        when(paymentRepository.findAllByRentalIn(rentals, Pageable.unpaged()))
                .thenReturn(payments);
        when(paymentMapper.toDto(pendingPayment)).thenReturn(pendingPaymentDto);
        when(paymentMapper.toDto(successfulPayment)).thenReturn(successfulRegularPayment);
        Page<PaymentDto> expected = new PageImpl<>(paymentDtos);

        Page<PaymentDto> actual = paymentService
                .findPaymentsByUserId(user.getId(), Pageable.unpaged(), user.getEmail());
        assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findByUserId(user.getId());
        verify(paymentRepository, times(1))
                .findAllByRentalIn(rentals, Pageable.unpaged());
    }

    @Test
    @DisplayName("Should throw an error due to invalid user")
    void findPaymentsByUserId_invalidUser_shouldThrowError() {
        long userId = 99L;
        String userEmail = "user@gmail.com";
        String expectedMessage = "User not found with email: " + userEmail;
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> paymentService.findPaymentsByUserId(userId, Pageable.unpaged(), userEmail));

        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should return payment dto with rental status confirmed")
    void getSuccessfulPayment_regularPayment_shouldReturnPaymentDto() {
        Payment payment = PaymentUtil.getPendingPayment();
        Rental rental = RentalUtil.getPendingRental();
        Rental confirmedRental = RentalUtil.getConfirmedRegularRental();
        PaymentDto expected = PaymentUtil.getSuccessfulRegularPaymentDto();

        when(paymentRepository.findBySessionId(payment.getSessionId()))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(rentalRepository.findById(payment.getId())).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(confirmedRental);
        doNothing().when(notificationService).sendNotification(anyString());
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentDto actual = paymentService
                .getSuccessfulPayment(payment.getSessionId());
        assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findBySessionId(payment.getSessionId());
        verify(paymentRepository, times(1)).save(payment);
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    @DisplayName("Should return payment dto with with rental status completed")
    void getSuccessfulPayment_finePayment_shouldReturnPaymentDto() {
        Rental rental = RentalUtil.getPendingFineRental();
        Payment payment = PaymentUtil.getPendingFinePayment();
        Rental completedRental = RentalUtil.getCompletedPendingFineRental();
        PaymentDto expected = PaymentUtil.getCompletedFinePaymentDto();
        Car car = CarUtil.getExistedCar();

        when(paymentRepository.findBySessionId(payment.getSessionId()))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(rentalRepository.findById(payment.getRental().getId()))
                .thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(completedRental);
        when(carRepository.save(car)).thenReturn(car);
        doNothing().when(notificationService).sendNotification(anyString());
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentDto actual = paymentService
                .getSuccessfulPayment(payment.getSessionId());
        assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findBySessionId(payment.getSessionId());
        verify(paymentRepository, times(1)).save(payment);
        verify(carRepository, times(1)).save(any(Car.class));
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    @DisplayName("Should throw an error due to payment status is paid")
    void getSuccessfulPayment_alreadyPaid_shouldThrowError() {
        Payment payment = PaymentUtil.getSuccessfulPayment();
        String expected = "Payment status: "
                + payment.getPaymentStatus() + " cannot continue";

        when(paymentRepository.findBySessionId(payment.getSessionId()))
                .thenReturn(Optional.of(payment));

        PaymentStatusException actual = assertThrows(PaymentStatusException.class,
                () -> paymentService.getSuccessfulPayment(payment.getSessionId()));

        assertEquals(expected, actual.getMessage());
        verify(paymentRepository, times(1)).findBySessionId(payment.getSessionId());
    }

    @Test
    @DisplayName("Should return payment dto of successfully canceled payment")
    void getCanceledPayment_successfulCanceled_shouldReturnPaymentDto() {
        Payment pendingPayment = PaymentUtil.getPendingPayment();
        Payment canceledPayment = PaymentUtil.getCancelPayment();
        Rental pendingRental = RentalUtil.getPendingRental();
        Rental canceledRental = RentalUtil.getCanceledRental();
        Car car = CarUtil.getExistedCar();
        PaymentDto expected = PaymentUtil.getCancelledPaymentDto();

        when(paymentRepository.findBySessionId(pendingPayment.getSessionId()))
                .thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(pendingPayment)).thenReturn(canceledPayment);
        when(rentalRepository.findById(pendingRental.getId()))
                .thenReturn(Optional.of(pendingRental));
        when(rentalRepository.save(pendingRental)).thenReturn(canceledRental);
        when(carRepository.save(car)).thenReturn(car);
        doNothing().when(notificationService).sendNotification(anyString());
        when(paymentMapper.toDto(canceledPayment)).thenReturn(expected);

        PaymentDto actual = paymentService
                .getCanceledPayment(pendingPayment.getSessionId());
        assertEquals(expected, actual);
        verify(paymentRepository, times(1))
                .findBySessionId(pendingPayment.getSessionId());
        verify(rentalRepository, times(1)).save(pendingRental);
        verify(carRepository, times(1)).save(any(Car.class));
        verify(notificationService, times(1)).sendNotification(anyString());
        verify(paymentMapper, times(1)).toDto(canceledPayment);

    }

    @Test
    @DisplayName("Should throw an error due to payment already canceled")
    void getCanceledPayment_alreadyCancelled_shouldThrowError() {
        Payment cancelledPayment = PaymentUtil.getCanceledPayment();
        String expected = "Payment status: "
                + cancelledPayment.getPaymentStatus() + " cannot cancel payment";

        when(paymentRepository.findBySessionId(cancelledPayment.getSessionId()))
                .thenReturn(Optional.of(cancelledPayment));

        PaymentStatusException actual = assertThrows(PaymentStatusException.class,
                () -> paymentService.getCanceledPayment(cancelledPayment.getSessionId()));

        assertEquals(expected, actual.getMessage());
        verify(paymentRepository, times(1))
                .findBySessionId(cancelledPayment.getSessionId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(rentalRepository, never()).save(any(Rental.class));
        verify(carRepository, never()).save(any(Car.class));
        verify(notificationService, never()).sendNotification(anyString());
    }
}
