package com.app.carsharing.service.notification;

import com.app.carsharing.model.Rental;
import com.app.carsharing.service.rental.RentalService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OverdueRentalScheduler {
    private final RentalService rentalService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkAndNotifyOverdueRental() {
        log.info("Starting schedule checking for overdue rentals...");
        List<Rental> overdueRentals = rentalService.getAllOverdueRentals();
        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification("No rentals overdue today!");
        } else {
            String notificationMessage = String.format("**%d OVERDUE RENTALS!** \n\n",
                    overdueRentals.size());
            for (Rental rental : overdueRentals) {
                String rentalInfo = String.format("- Car: %s (ID: %d) "
                        + "\n- Due Date: %s"
                                + "\n- Days overdue: %d\n\n",
                        rental.getCar().getModel(),
                        rental.getId(),
                        rental.getReturnDate(),
                        ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now()));
                notificationMessage = System.lineSeparator() + rentalInfo + System.lineSeparator();
            }
            notificationService.sendNotification(notificationMessage);
        }
    }
}
