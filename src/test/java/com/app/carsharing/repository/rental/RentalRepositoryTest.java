package com.app.carsharing.repository.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.carsharing.model.Rental;
import com.app.carsharing.model.User;
import java.util.Collections;
import java.util.List;
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
class RentalRepositoryTest {
    private final RentalRepository rentalRepository;

    @Autowired
    public RentalRepositoryTest(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Test
    @DisplayName("Searches for user's leases by statuses. "
            + "Should return a list of leases that match specific lease statuses.")
    void findByUserIdAndRentalStatusIn_validUserAndStatus_shouldReturnRental() {
        User user = new User();
        user.setId(1L);
        List<Rental.RentalStatus> expected = List.of(
                Rental.RentalStatus.PENDING,
                Rental.RentalStatus.CONFIRMED);

        List<Rental> rentals = rentalRepository
                .findByUserIdAndRentalStatusIn(user.getId(), expected);

        assertEquals(3, rentals.size());
        List<Rental.RentalStatus> actual = rentals.stream().map(Rental::getRentalStatus).toList();
        assertTrue(actual.stream()
                .anyMatch(rentalStatus -> rentalStatus.equals(expected.getFirst())));
        assertTrue(actual.stream()
                .anyMatch(rentalStatus -> rentalStatus.equals(expected.get(1))));
    }

    @Test
    @DisplayName("Should return an empty list for a user who is not in the database")
    void findByUserIdAndRentalStatusIn_invalidUser_shouldReturnEmptyList() {
        User user = new User();
        user.setId(4L);
        List<Rental.RentalStatus> rentalStatuses = List.of(
                Rental.RentalStatus.PENDING,
                Rental.RentalStatus.CONFIRMED);

        List<Rental> actual = rentalRepository
                .findByUserIdAndRentalStatusIn(user.getId(), rentalStatuses);
        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    @DisplayName("A confirmed rental must be returned,"
            + " the return date of which is overdue.")
    void findAllOverdueRentals_matchOverdueRental_shouldReturnRentals() {
        List<Rental.RentalStatus> expected = List.of(Rental.RentalStatus.PENDING,
                Rental.RentalStatus.CONFIRMED);

        List<Rental> allOverdueRentals = rentalRepository.findAllOverdueRentals(expected);
        assertEquals(1, allOverdueRentals.size());
        Rental rental = allOverdueRentals.getFirst();
        assertEquals(4L, rental.getId());
        assertTrue(expected.contains(rental.getRentalStatus()));
    }

    @Test
    @DisplayName("Should return an empty list when searching on a status"
            + " that does not match overdue rent.")
    void findAllOverdueRentals_noMatchedRentals_shouldReturnEmptyList() {
        List<Rental.RentalStatus> expected = List.of(Rental.RentalStatus.CANCELED,
                Rental.RentalStatus.COMPLETED);
        List<Rental> allOverdueRentals = rentalRepository.findAllOverdueRentals(expected);
        assertEquals(Collections.emptyList(), allOverdueRentals);
    }
}
