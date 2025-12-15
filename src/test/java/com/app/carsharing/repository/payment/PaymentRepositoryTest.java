package com.app.carsharing.repository.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.carsharing.model.Payment;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {
        "classpath:database/cars/delete_cars.sql",
        "classpath:database/users/delete_user.sql",
        "classpath:database/rentals/delete_rentals.sql",
        "classpath:database/payments/delete_payments.sql",
        "classpath:database/cars/insert_4_cars.sql",
        "classpath:database/users/insert_users.sql",
        "classpath:database/rentals/insert_6_rentals.sql",
        "classpath:database/payments/insert_4_payments.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/payments/delete_payments.sql",
        "classpath:database/rentals/delete_rentals.sql",
        "classpath:database/cars/delete_cars.sql",
        "classpath:database/users/delete_user.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {
    private final PaymentRepository paymentRepository;

    @Autowired
    PaymentRepositoryTest(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Test
    @DisplayName("Should return payment with an unique session id")
    void findBySessionId_validSessionId_shouldReturnCorrectPayment() {
        String sessionId = "cs_test_session12345";

        Optional<Payment> actual = paymentRepository.findBySessionId(sessionId);
        assertTrue(actual.isPresent());
        assertEquals(sessionId, actual.get().getSessionId());

    }

    @Test
    @DisplayName("Should return an empty optional due to insert invalid session id")
    void findBySessionId_invalidSessionId_shouldReturnEmptyPayment() {
        String sessionId = "cs_test_session00000";

        Optional<Payment> bySessionId = paymentRepository.findBySessionId(sessionId);

        assertTrue(bySessionId.isEmpty());
    }

    @Test
    @DisplayName("Should return payments with concrete payment status and rental id")
    void findAllByRentalIdAndPaymentStatus_validRentalIdAndStatus_shouldReturnCorrectPayment() {
        Long rentalId = 1L;
        Payment.Status status = Payment.Status.PENDING;

        List<Payment> actual = paymentRepository
                .findAllByRentalIdAndPaymentStatus(rentalId, status);
        assertEquals(1, actual.size());
        Payment actualPayment = actual.getFirst();

        assertEquals(status, actualPayment.getPaymentStatus());
        assertEquals(rentalId, actualPayment.getRental().getId());
    }

    @Test
    @DisplayName("Should return an empty list for unmatched payment status")
    void findAllByRentalIdAndPaymentStatus_noMatchedPaymentStatus_shouldReturnEmptyLst() {
        Long rentalId = 1L;
        Payment.Status status = Payment.Status.CANCELED;

        List<Payment> allByRentalIdAndPaymentStatus = paymentRepository
                .findAllByRentalIdAndPaymentStatus(rentalId, status);

        assertTrue(allByRentalIdAndPaymentStatus.isEmpty());
    }
}
