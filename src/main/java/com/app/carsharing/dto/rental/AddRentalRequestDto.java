package com.app.carsharing.dto.rental;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddRentalRequestDto(
        @NotNull
        @FutureOrPresent
        LocalDate rentalDate,
        @NotNull
        @Future
        LocalDate returnDate,
        @NotNull
        Long carId
) {
}
