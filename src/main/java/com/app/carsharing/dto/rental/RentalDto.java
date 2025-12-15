package com.app.carsharing.dto.rental;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

public record RentalDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long userId,
        Long carId,
        String rentalStatus,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String sessionUrl
) {
}
