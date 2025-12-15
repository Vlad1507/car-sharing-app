package com.app.carsharing.service.rental;

import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalDto addRental(String userEmail, AddRentalRequestDto rentalRequestDto);

    RentalDto findRentalById(Long id);

    RentalDto endRental(Long rentalId, LocalDate actualReturnDate);

    Page<RentalDto> getActiveRentalByUserId(Long userId, boolean isActive, Pageable pageable);

    void deleteRental(Long rentalId, String userEmail);

    List<Rental> getAllOverdueRentals();
}
