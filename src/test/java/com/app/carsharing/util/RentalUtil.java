package com.app.carsharing.util;

import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.model.Rental;
import java.time.LocalDate;

public class RentalUtil {
    public static Rental getPendingRental() {
        Rental pendingRental = new Rental();
        pendingRental.setId(1L);
        pendingRental.setRentalDate(LocalDate.of(2025, 11, 1));
        pendingRental.setReturnDate(LocalDate.of(2025, 11, 10));
        pendingRental.setActualReturnDate(null);
        pendingRental.setUser(UserUtil.getExistedUser());
        pendingRental.setCar(CarUtil.getExistedCar());
        pendingRental.setRentalStatus(Rental.RentalStatus.PENDING);
        return pendingRental;
    }

    public static Rental getConfirmedRegularRental() {
        Rental pendingRental = new Rental();
        pendingRental.setId(1L);
        pendingRental.setRentalDate(LocalDate.of(2025, 11, 1));
        pendingRental.setReturnDate(LocalDate.of(2025, 11, 10));
        pendingRental.setActualReturnDate(null);
        pendingRental.setUser(UserUtil.getExistedUser());
        pendingRental.setCar(CarUtil.getExistedCar());
        pendingRental.setRentalStatus(Rental.RentalStatus.CONFIRMED);
        return pendingRental;
    }

    public static Rental getPendingFineRental() {
        Rental pendingRental = new Rental();
        pendingRental.setId(2L);
        pendingRental.setRentalDate(LocalDate.of(2025, 11, 1));
        pendingRental.setReturnDate(LocalDate.of(2025, 11, 10));
        pendingRental.setActualReturnDate(LocalDate.of(2025, 11, 15));
        pendingRental.setUser(UserUtil.getExistedUser());
        pendingRental.setCar(CarUtil.getExistedCar());
        pendingRental.setRentalStatus(Rental.RentalStatus.PENDING_FINE);
        return pendingRental;
    }

    public static Rental getCompletedPendingFineRental() {
        Rental pendingRental = new Rental();
        pendingRental.setId(2L);
        pendingRental.setRentalDate(LocalDate.of(2025, 11, 1));
        pendingRental.setReturnDate(LocalDate.of(2025, 11, 10));
        pendingRental.setActualReturnDate(LocalDate.of(2025, 11, 15));
        pendingRental.setUser(UserUtil.getExistedUser());
        pendingRental.setCar(CarUtil.getExistedCar());
        pendingRental.setRentalStatus(Rental.RentalStatus.PENDING_FINE);
        return pendingRental;
    }

    public static Rental getConfirmedRental() {
        Rental confirmedRental = new Rental();
        confirmedRental.setId(2L);
        confirmedRental.setRentalDate(LocalDate.now());
        confirmedRental.setReturnDate(LocalDate.now().plusDays(10));
        confirmedRental.setActualReturnDate(null);
        confirmedRental.setUser(UserUtil.getExistedUser());
        confirmedRental.setCar(CarUtil.getSecondExistedCar());
        confirmedRental.setRentalStatus(Rental.RentalStatus.CONFIRMED);
        return confirmedRental;
    }

    public static AddRentalRequestDto getRentalRequestDto() {
        return new AddRentalRequestDto(
                LocalDate.now(),
                LocalDate.now().plusDays(9),
                1L
        );
    }

    public static Rental getRentalFromRequest(AddRentalRequestDto rentalRequestDto) {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(rentalRequestDto.rentalDate());
        rental.setReturnDate(rentalRequestDto.returnDate());
        rental.setActualReturnDate(null);
        rental.setUser(UserUtil.getExistedUser());
        rental.setCar(CarUtil.getExistedCar());
        rental.setRentalStatus(Rental.RentalStatus.PENDING);
        return rental;
    }

    public static RentalDto getRentalDto() {
        return new RentalDto(
                1L,
                LocalDate.now(),
                LocalDate.now().plusDays(9),
                null,
                1L,
                1L,
                "PENDING",
                null
        );
    }

    public static RentalDto getConfirmedRentalDto() {
        return new RentalDto(
                2L,
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 10),
                null,
                1L,
                1L,
                "CONFIRMED",
                null
        );
    }

    public static RentalDto getInTimeRentalDto() {
        return new RentalDto(
                2L,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                LocalDate.now(),
                2L,
                2L,
                "COMPLETED",
                null
        );
    }

    public static RentalDto getOverdueRentalDto() {
        return new RentalDto(
                2L,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15),
                2L,
                2L,
                "PENDING_FINE",
                null
        );
    }

    public static RentalDto getLateReturnRentalDto() {
        return new RentalDto(
                5L,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                1L,
                1L,
                "PENDING_FINE",
                "https://checkout.stripe.com/c/pay/cs_test_a1afSvfMLlaxiDy1atB"
                        + "SzFhk3pVc5hGtJM0UxPZfL491UXHyvcAY6QmUKk#fidnandhYHdWcXxpYCc%2"
                        + "FJ2FgY2RwaXEnKSdkdWxOYHwnPyd1blpxYHZxWjA0VkRnU25DfGhUQ3FcMjxqS"
                        + "j03UXZUSWxwfFUyVEBdbEFhVkpKTjd0bldxf1E3SGhTdVVIYlJUSE1TcH0wUUx"
                        + "daGJtaWcwMV10cHJrMn00bWtqPUhCd1doNTUzNmNncX9TNicpJ2N3amhWYHdzYH"
                        + "cnP3F3cGApJ2dkZm5id2pwa2FGamlqdyc%2FJyZjY2NjY2MnKSdpZHxqcHFRfHVg"
                        + "Jz8ndmxrYmlgWmxxYGgnKSdga2RnaWBVaWRmYG1qaWFgd3YnP3F3cGB4JSUl"
        );
    }

    public static Rental getReturnedRental() {
        Rental returnedRental = new Rental();
        returnedRental.setId(1L);
        returnedRental.setRentalDate(LocalDate.of(2025, 11, 1));
        returnedRental.setReturnDate(LocalDate.of(2025, 11, 10));
        returnedRental.setActualReturnDate(LocalDate.of(2025, 11, 10));
        returnedRental.setUser(UserUtil.getExistedUser());
        returnedRental.setCar(CarUtil.getExistedCar());
        returnedRental.setRentalStatus(Rental.RentalStatus.COMPLETED);
        return returnedRental;
    }

    public static Rental getCanceledRental() {
        Rental canceledRental = new Rental();
        canceledRental.setId(1L);
        canceledRental.setRentalDate(LocalDate.of(2025, 11, 1));
        canceledRental.setReturnDate(LocalDate.of(2025, 11, 10));
        canceledRental.setActualReturnDate(null);
        canceledRental.setUser(UserUtil.getExistedUser());
        canceledRental.setCar(CarUtil.getExistedCar());
        canceledRental.setRentalStatus(Rental.RentalStatus.CANCELED);
        return canceledRental;
    }
}
