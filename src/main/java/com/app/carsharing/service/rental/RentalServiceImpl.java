package com.app.carsharing.service.rental;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.exception.RentalAccessDeniedException;
import com.app.carsharing.exception.RentalException;
import com.app.carsharing.mapper.RentalMapper;
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
import com.app.carsharing.service.payment.PaymentService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    public static final String FINE = "FINE";
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public RentalDto addRental(String userEmail, AddRentalRequestDto rentalRequestDto) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't get user by email: " + userEmail));
        List<Rental> rentals = rentalRepository.findByUserId(user.getId());
        boolean rentalWithSameStatus = rentals.stream()
                .anyMatch(rental ->
                        rental.getRentalStatus() == Rental.RentalStatus.PENDING);
        if (rentalWithSameStatus) {
            throw new RentalException("Pending rental already exists. "
                    + "Please complete or cancel it first.");
        }
        Car car = getCar(rentalRequestDto.carId());
        if (car.getAvailableCars() > 0) {
            car.setAvailableCars(car.getAvailableCars() - 1);
            carRepository.save(car);
            Rental rental = rentalMapper.toModel(rentalRequestDto);
            rental.setUser(user);
            rental.setRentalStatus(Rental.RentalStatus.PENDING);
            Rental savedRental = rentalRepository.save(rental);
            String message = String.format(
                    "***NEW RENTAL CREATED***\n"
                            + "Car: %s (ID: %d)\n"
                            + "User: %s (ID: %d)\n"
                            + "Due Date: %s",
                    car.getModel(),
                    car.getId(),
                    savedRental.getUser().getEmail(),
                    savedRental.getUser().getId(),
                    savedRental.getReturnDate());
            notificationService.sendNotification(message);
            return rentalMapper.toDto(savedRental);
        } else {
            throw new EntityNotFoundException("No cars: " + car.getModel() + " available");
        }
    }

    @Override
    public RentalDto findRentalById(Long id) {
        Rental rental = getRental(id);
        return rentalMapper.toDto(rental);
    }

    @Override
    public RentalDto endRental(Long rentalId, LocalDate actualReturnDate) {
        Rental rental = getRental(rentalId);
        if (rental.getActualReturnDate() != null) {
            throw new RentalException("Rental by id: "
                    + rentalId + " already returned");
        }
        rental.setActualReturnDate(actualReturnDate);
        if (actualReturnDate.isAfter(rental.getReturnDate())) {
            CreatePaymentRequestSessionDto fineRequest =
                    new CreatePaymentRequestSessionDto(FINE, rentalId);
            PaymentDto finePayment = paymentService.createPayment(fineRequest);
            rental.setRentalStatus(Rental.RentalStatus.PENDING_FINE);
            Rental savedRental = rentalRepository.save(rental);
            String message = String.format(
                    "***LATE RETURN - FINE REQUESTED!***\n"
                            + "Rental ID: %d\n"
                            + "Customer: %s \n"
                            + "Due Date: %s\n"
                            + "Actual Return Date: %s\n"
                            + "Rental Status: %s",
                    savedRental.getId(),
                    savedRental.getUser().getEmail(),
                    savedRental.getRentalDate(),
                    savedRental.getActualReturnDate(),
                    savedRental.getRentalStatus());
            notificationService.sendNotification(message);
            return rentalMapper.toDtoWithPaymentUrl(rental, finePayment.sessionUrl());
        } else {
            Car car = getCar(rental.getCar().getId());
            car.setAvailableCars(car.getAvailableCars() + 1);
            carRepository.save(car);
            rental.setRentalStatus(Rental.RentalStatus.COMPLETED);
            Rental savedRental = rentalRepository.save(rental);
            String message = String.format(
                    "***CAR RETURNED***\n"
                            + "Rental ID: %d\n"
                            + "Car: %s (ID: %d)\n"
                            + "Actual Return Date: %s",
                    savedRental.getId(),
                    savedRental.getCar().getModel(),
                    savedRental.getCar().getId(),
                    savedRental.getActualReturnDate());
            notificationService.sendNotification(message);
            return rentalMapper.toDto(savedRental);
        }
    }

    @Override
    public Page<RentalDto> getActiveRentalByUserId(
            Long userId,
            boolean isActive,
            Pageable pageable
    ) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User by " + userId + " not found"));
        List<Rental> rentals;
        if (isActive) {
            rentals = rentalRepository.findByUserIdAndRentalStatusIn(userId,
                    List.of(Rental.RentalStatus.PENDING,
                            Rental.RentalStatus.PENDING_FINE,
                            Rental.RentalStatus.CONFIRMED));
        } else {
            rentals = rentalRepository.findByUserIdAndRentalStatusIn(userId,
                    List.of(Rental.RentalStatus.CANCELED, Rental.RentalStatus.COMPLETED));
        }
        List<RentalDto> rentalDtos = rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
        return new PageImpl<>(rentalDtos, pageable, rentalDtos.size());
    }

    @Override
    public void deleteRental(Long rentalId, String userEmail) {
        Rental rental = getRental(rentalId);
        User user = getUser(userEmail);
        boolean isAdminOrManager = isUserAdminOrManager(user);
        boolean isOwner = isRentalOwnedBy(rentalId, user);
        if (!isAdminOrManager && !isOwner) {
            throw new RentalAccessDeniedException("User with email "
                    + userEmail + " not owning that rental");
        }
        if (rental.getRentalStatus() != Rental.RentalStatus.PENDING) {
            throw new RentalException("Rental by id: " + rentalId
                    + " cannot be cancelled as it is: " + rental.getRentalStatus());
        }
        List<Payment> paymentsToDelete = paymentRepository
                .findAllByRentalIdAndPaymentStatus(rentalId, Payment.Status.PENDING);
        paymentRepository.deleteAll(paymentsToDelete);
        Car car = rental.getCar();
        car.setAvailableCars(car.getAvailableCars() + 1);
        carRepository.save(car);
        rentalRepository.delete(rental);
        String message = String.format(
                "***RENTAL DELETED***\n"
                        + "Rental ID: %d\n"
                        + "Car: %s (ID: %d)",
                rental.getId(),
                car.getModel(),
                car.getId());
        notificationService.sendNotification(message);
    }

    private boolean isUserAdminOrManager(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(Role.RoleName.ROLE_MANAGER)
                        || role.getRoleName().equals(Role.RoleName.ROLE_ADMIN));
    }

    private boolean isRentalOwnedBy(Long rentalId, User user) {
        return rentalRepository.findById(rentalId)
                .map(rental -> rental.getUser().getId().equals(user.getId()))
                .orElse(false);
    }

    @Override
    public List<Rental> getAllOverdueRentals() {
        List<Rental.RentalStatus> activeStatuses =
                List.of(Rental.RentalStatus.PENDING, Rental.RentalStatus.CONFIRMED);
        return rentalRepository.findAllOverdueRentals(activeStatuses);
    }

    private Car getCar(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car with car id: "
                        + id + " not found")
        );
    }

    private Rental getRental(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Rental by rental id: "
                                + rentalId + " not found"));
    }

    private User getUser(String username) {
        return userRepository.findByEmail(username).orElseThrow(
                () -> new EntityNotFoundException("User by " + username + " not found"));
    }
}
