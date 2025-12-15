package com.app.carsharing.service.payment;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.exception.InvalidRentalDurationException;
import com.app.carsharing.exception.PaymentAccessDeniedException;
import com.app.carsharing.exception.PaymentNotFoundException;
import com.app.carsharing.exception.PaymentStatusException;
import com.app.carsharing.exception.PaymentTypeException;
import com.app.carsharing.mapper.PaymentMapper;
import com.app.carsharing.model.Car;
import com.app.carsharing.model.Payment;
import com.app.carsharing.model.Rental;
import com.app.carsharing.model.Role;
import com.app.carsharing.model.User;
import com.app.carsharing.repository.car.CarRepository;
import com.app.carsharing.repository.payment.PaymentRepository;
import com.app.carsharing.repository.rental.RentalRepository;
import com.app.carsharing.repository.user.UserRepository;
import com.app.carsharing.service.notification.NotificationService;
import com.app.carsharing.service.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final BigDecimal HUNDRED_MULTIPLIER = BigDecimal.valueOf(100L);
    private static final String EUR_CURRENCY_CODE = "EUR";
    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of("BIF", "CLP", "DJF", "GNF",
            "JPY", "KMF", "KRW", "MGA", "PYG", "RWF", "UGX", "VND", "VUV", "XAF", "XOF", "XPF");
    private static final String BASIC_URL = "http://localhost:8080/";
    private static final String SUCCESS_ENDPOINT =
            "payments/successful?session_id={CHECKOUT_SESSION_ID}";
    private static final String CANCEL_ENDPOINT =
            "payments/canceled?session_id={CHECKOUT_SESSION_ID}";
    private static final BigDecimal OVERUSE_MULTIPLIER = new BigDecimal("0.20");

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final StripeClient stripeClient;
    private final CarRepository carRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public PaymentDto createPayment(CreatePaymentRequestSessionDto requestSessionDto) {
        Rental rental = getRental(requestSessionDto.rentalId());
        String paymentType = requestSessionDto.paymentType().toUpperCase();
        BigDecimal calculatedAmount = switch (paymentType) {
            case "PAYMENT" -> calculateRentalFeeAmount(rental);
            case "FINE" -> calculateFineAmountToPay(rental);
            default -> throw new PaymentTypeException("Unsupported or invalid payment type: "
                    + paymentType);
        };
        Payment payment = getPayment(requestSessionDto, rental, paymentType, calculatedAmount);
        return paymentMapper.toDto(payment);
    }

    @Override
    public Page<PaymentDto> findPaymentsByUserId(Long userId, Pageable pageable, String username) {
        User user = getUser(username);
        boolean isOwner = user.getId().equals(userId);
        boolean isAdminOrManager = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(Role.RoleName.ROLE_ADMIN)
                        || role.getRoleName().equals(Role.RoleName.ROLE_MANAGER));
        if (!isAdminOrManager && !isOwner) {
            throw new PaymentAccessDeniedException("User with email "
                    + username + " has no rights to access those payments");
        }
        List<Rental> rentals = rentalRepository.findByUserId(userId);
        if (rentals.isEmpty()) {
            return Page.empty();
        }
        Page<Payment> payments = paymentRepository.findAllByRentalIn(rentals, pageable);
        List<PaymentDto> paymentDtoList = payments.stream().map(paymentMapper::toDto).toList();
        return new PageImpl<>(paymentDtoList, pageable, payments.getTotalElements());
    }

    @Override
    public PaymentDto getSuccessfulPayment(String sessionId) {
        Payment payment = getPaymentFromSessionId(sessionId);
        if (payment.getPaymentStatus() != Payment.Status.PENDING) {
            throw new PaymentStatusException("Payment status: "
                    + payment.getPaymentStatus() + " cannot continue");
        }
        payment.setPaymentStatus(Payment.Status.PAID);
        payment = paymentRepository.save(payment);
        String message;
        if (payment.getPaymentType() == Payment.PaymentType.FINE) {
            updateRentalStatus(payment.getRental().getId(), Rental.RentalStatus.COMPLETED);
            message = String.format("***FINE PAYMENT SUCCESSFUL***\n"
                            + "Amount: %.2f\n"
                            + "Rental ID: %d\n"
                            + "Customer: %s",
                    payment.getAmountToPay(),
                    payment.getRental().getId(),
                    payment.getRental().getUser().getEmail());
        } else {
            updateRentalStatus(payment.getRental().getId(), Rental.RentalStatus.CONFIRMED);
            message = String.format("***NEW RENTAL CONFIRMED***\n"
                            + "Amount: %.2f\n"
                            + "Rental ID: %d\n"
                            + "Due date: %s\n"
                            + "Customer: %s",
                    payment.getAmountToPay(),
                    payment.getRental().getId(),
                    payment.getRental().getReturnDate(),
                    payment.getRental().getUser().getEmail());
        }
        notificationService.sendNotification(message);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto getCanceledPayment(String sessionId) {
        Payment payment = getPaymentFromSessionId(sessionId);
        if (payment.getPaymentStatus() != Payment.Status.PENDING) {
            throw new PaymentStatusException("Payment status: "
                    + payment.getPaymentStatus() + " cannot cancel payment");
        }
        payment.setPaymentStatus(Payment.Status.CANCELED);
        payment = paymentRepository.save(payment);
        updateRentalStatus(payment.getRental().getId(),
                Rental.RentalStatus.CANCELED);
        String message = String.format("***CANCELED PAYMENT SUCCESSFUL***\n"
                        + "Amount: %.2f\n"
                        + "Rental ID: %d\n"
                        + "Customer: %s\n",
                payment.getAmountToPay(),
                payment.getRental().getId(),
                payment.getRental().getUser().getEmail());
        notificationService.sendNotification(message);
        return paymentMapper.toDto(payment);
    }

    private Payment getPayment(
            CreatePaymentRequestSessionDto requestSessionDto,
            Rental rental,
            String paymentType,
            BigDecimal calculatedAmount
    ) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setPaymentType(Payment.PaymentType.valueOf(paymentType));
        payment.setPaymentStatus(Payment.Status.PENDING);
        payment.setAmountToPay(calculatedAmount);
        Long amount = convertToStripeAmount(calculatedAmount, EUR_CURRENCY_CODE);
        String successUrl = BASIC_URL + SUCCESS_ENDPOINT;
        String canceledUrl = BASIC_URL + CANCEL_ENDPOINT;
        try {
            Session sessionCheckout = stripeClient.createSessionCheckout(
                    amount,
                    EUR_CURRENCY_CODE,
                    successUrl,
                    canceledUrl,
                    requestSessionDto.rentalId().toString());
            payment.setSessionId(sessionCheckout.getId());
            payment.setSessionUrl(new URI(sessionCheckout.getUrl()).toURL());
        } catch (StripeException | MalformedURLException | URISyntaxException e) {
            throw new RuntimeException("Failed to create Stripe checkout session", e);
        }
        payment = paymentRepository.save(payment);
        return payment;
    }

    private User getUser(String username) {
        return userRepository.findByEmail(username).orElseThrow(
                () -> new EntityNotFoundException("User not found with email: " + username)
        );
    }

    private void updateRentalStatus(Long rentalId, Rental.RentalStatus rentalStatus) {
        Rental rental = getRental(rentalId);
        rental.setRentalStatus(rentalStatus);
        rentalRepository.save(rental);
        if (rentalStatus == Rental.RentalStatus.CANCELED
                || rentalStatus == Rental.RentalStatus.COMPLETED) {
            Car car = rental.getCar();
            car.setAvailableCars(car.getAvailableCars() + 1);
            carRepository.save(car);
        }
    }

    private Payment getPaymentFromSessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new PaymentNotFoundException("No payment found for sessionId: " + sessionId)
        );
    }

    private BigDecimal calculateRentalFeeAmount(Rental rental) {
        Car car = rental.getCar();
        BigDecimal dailyFee = car.getDailyFee();
        LocalDate endDate = rental.getReturnDate().plusDays(1);
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), endDate);
        if (days <= 0) {
            throw new InvalidRentalDurationException("Rental duration must be at least 1 day");
        }
        return dailyFee.multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal calculateFineAmountToPay(Rental rental) {
        LocalDate returnDate = rental.getReturnDate();
        LocalDate actualReturnDate = rental.getActualReturnDate();
        if (actualReturnDate == null || !actualReturnDate.isAfter(returnDate)) {
            throw new InvalidRentalDurationException("Fine calculation requested "
                    + "but car was not returned late for Rental id: " + rental.getId());
        }
        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(),
                rental.getActualReturnDate());
        if (overdueDays <= 0) {
            throw new InvalidRentalDurationException("Overdue days calculated as zero or less");
        }
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        BigDecimal extraDailyFee = dailyFee.multiply(OVERUSE_MULTIPLIER);
        BigDecimal result = dailyFee.add(extraDailyFee);
        return result.multiply(BigDecimal.valueOf(overdueDays));
    }

    private Long convertToStripeAmount(BigDecimal amount, String currency) {
        String currencyUpperCase = currency.toUpperCase();
        if (ZERO_DECIMAL_CURRENCIES.contains(currencyUpperCase)) {
            return amount.longValue();
        }
        return amount.multiply(HUNDRED_MULTIPLIER).longValue();
    }

    private Rental getRental(Long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id " + rentalId + " does not exist")
        );
    }
}
