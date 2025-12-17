package com.app.carsharing.repository.payment;

import com.app.carsharing.model.Payment;
import com.app.carsharing.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAllByRentalIn(List<Rental> rentals, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByRentalIdAndPaymentStatus(Long rentalId, Payment.Status status);
}
