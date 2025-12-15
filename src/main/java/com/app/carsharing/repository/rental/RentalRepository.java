package com.app.carsharing.repository.rental;

import com.app.carsharing.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);

    List<Rental> findByUserIdAndRentalStatusIn(
            Long userId,
            List<Rental.RentalStatus> rentalStatuses
    );

    @EntityGraph(attributePaths = "car")
    Optional<Rental> findById(Long rentalId);

    @Query("SELECT r FROM Rental r JOIN FETCH r.user WHERE r.returnDate <= CURRENT_DATE "
            + "AND r.rentalStatus IN :rentalStatuses")
    List<Rental> findAllOverdueRentals(
            @Param("rentalStatuses") List<Rental.RentalStatus> rentalStatuses
    );
}
