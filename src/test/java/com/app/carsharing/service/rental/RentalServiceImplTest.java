package com.app.carsharing.service.rental;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.exception.RentalException;
import com.app.carsharing.mapper.RentalMapper;
import com.app.carsharing.model.Car;
import com.app.carsharing.model.Payment;
import com.app.carsharing.model.Rental;
import com.app.carsharing.model.User;
import com.app.carsharing.repository.car.CarRepository;
import com.app.carsharing.repository.payment.PaymentRepository;
import com.app.carsharing.repository.rental.RentalRepository;
import com.app.carsharing.repository.user.UserRepository;
import com.app.carsharing.service.notification.NotificationService;
import com.app.carsharing.service.payment.PaymentService;
import com.app.carsharing.util.CarUtil;
import com.app.carsharing.util.PaymentUtil;
import com.app.carsharing.util.RentalUtil;
import com.app.carsharing.util.UserUtil;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RentalServiceImplTest {
    @MockitoBean
    private final RentalRepository rentalRepository;
    @MockitoBean
    private final RentalMapper rentalMapper;
    @MockitoBean
    private final CarRepository carRepository;
    @MockitoBean
    private final NotificationService notificationService;
    @MockitoBean
    private final PaymentService paymentService;
    @MockitoBean
    private final PaymentRepository paymentRepository;
    @MockitoBean
    private final UserRepository userRepository;
    private final RentalService rentalService;

    @Autowired
    public RentalServiceImplTest(RentalService rentalService,
                                 RentalMapper rentalMapper,
                                 RentalRepository rentalRepository,
                                 CarRepository carRepository,
                                 NotificationService notificationService,
                                 PaymentService paymentService,
                                 PaymentRepository paymentRepository,
                                 UserRepository userRepository) {
        this.rentalService = rentalService;
        this.rentalMapper = rentalMapper;
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("Should create a new rental when the car is available")
    void addRental_carAvailable_shouldSaveAndReturnRental() {
        User existedUser = UserUtil.getExistedUser();
        List<Rental> emptyPendingRentalList = Collections.emptyList();
        Car car = CarUtil.getExistedCar();
        AddRentalRequestDto rentalRequestDto = RentalUtil.getRentalRequestDto();
        Rental rental = RentalUtil.getRentalFromRequest(rentalRequestDto);
        RentalDto expected = RentalUtil.getRentalDto();

        when(userRepository.findByEmail(existedUser.getEmail()))
                .thenReturn(Optional.of(existedUser));
        when(rentalRepository.findByUserId(existedUser.getId()))
                .thenReturn(emptyPendingRentalList);
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalMapper.toModel(rentalRequestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalDto actual = rentalService.addRental(existedUser.getEmail(), rentalRequestDto);
        assertEquals(expected, actual);
        verify(rentalRepository).save(rental);
        verify(carRepository).save(car);
        verify(notificationService, times(1)).sendNotification(any());
    }

    @Test
    @DisplayName("Should throw an error due to an available rental with status PENDING")
    void addRental_duplicateStatus_shouldThrowError() {
        AddRentalRequestDto requestDto = RentalUtil.getRentalRequestDto();
        Rental rental = RentalUtil.getPendingRental();
        List<Rental> rentalList = List.of(rental);
        User user = UserUtil.getExistedUser();
        String expectedMessage = "Pending rental already exists. "
                + "Please complete or cancel it first.";

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserId(user.getId())).thenReturn(rentalList);

        RentalException actual = assertThrows(RentalException.class,
                () -> rentalService.addRental(user.getEmail(), requestDto));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should throw an error due to no available cars of a concrete model")
    void addRental_noAvailableCars_shouldThrowError() {
        AddRentalRequestDto requestDto = RentalUtil.getRentalRequestDto();
        User user = UserUtil.getExistedUser();
        List<Rental> emptyPendingRentalList = Collections.emptyList();
        Car noAvailableCar = CarUtil.getNoAvailableCar();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserId(user.getId())).thenReturn(emptyPendingRentalList);
        when(carRepository.findById(noAvailableCar.getId()))
                .thenReturn(Optional.of(noAvailableCar));

        String expectedMessage = "No cars: " + noAvailableCar.getModel() + " available";
        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> rentalService.addRental(user.getEmail(), requestDto));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should throw an error because rental has already been returned")
    void endRental_alreadyReturned_shouldThrowError() {
        Rental rental = RentalUtil.getReturnedRental();
        String expectedMessage = "Rental by id: " + rental.getId() + " already returned";

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        RentalException actual = assertThrows(RentalException.class,
                () -> rentalService.endRental(rental.getId(), rental.getActualReturnDate()));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should return RentalDto with fine payment session url")
    void endRental_overdueRental_shouldReturnRentalDtoWithPaymentUrl() {
        Rental rental = RentalUtil.getConfirmedRental();
        CreatePaymentRequestSessionDto fineRequest = new CreatePaymentRequestSessionDto(
                "FINE",
                rental.getId()
        );
        PaymentDto finePayment = PaymentUtil.getFinePaymentDtoForRental();
        RentalDto expected = RentalUtil.getOverdueRentalDto();

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(paymentService.createPayment(fineRequest)).thenReturn(finePayment);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDtoWithPaymentUrl(rental, finePayment.sessionUrl()))
                .thenReturn(expected);
        doNothing().when(notificationService).sendNotification(anyString());

        RentalDto actual = rentalService.endRental(expected.id(), expected.actualReturnDate());
        assertEquals(expected, actual);
        verify(paymentService).createPayment(fineRequest);
        verify(rentalRepository).save(rental);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("Should return RentalDto with rental status completed")
    void endRental_returnedInTimeRental_shouldReturnRentalDto() {
        Rental rental = RentalUtil.getConfirmedRental();
        Car car = CarUtil.getSecondExistedCar();
        RentalDto expected = RentalUtil.getInTimeRentalDto();

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalRepository.save(rental)).thenReturn(rental);
        doNothing().when(notificationService).sendNotification(anyString());
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalDto actual = rentalService.endRental(expected.id(), expected.actualReturnDate());
        assertEquals(expected.rentalStatus(), actual.rentalStatus());
        verify(carRepository).save(car);
        verify(rentalRepository).save(rental);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("Should return a page of rentals "
            + "where rental status can be pending, pending fine or confirmed")
    void getActiveRentalByUserId_activeRentals_shouldReturnActiveRentals() {
        User user = UserUtil.getExistedUser();
        boolean isActive = true;
        List<Rental.RentalStatus> activeRentals = List.of(
                Rental.RentalStatus.PENDING,
                Rental.RentalStatus.PENDING_FINE,
                Rental.RentalStatus.CONFIRMED);
        Rental pendingRental = RentalUtil.getPendingRental();
        Rental confirmedRental = RentalUtil.getConfirmedRental();
        List<Rental> rentalList = List.of(pendingRental, confirmedRental);
        RentalDto pendingRentalDto = RentalUtil.getRentalDto();
        RentalDto confirmedRentalDto = RentalUtil.getConfirmedRentalDto();
        List<RentalDto> expectedDtos = List.of(pendingRentalDto, confirmedRentalDto);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserIdAndRentalStatusIn(user.getId(), activeRentals))
                .thenReturn(rentalList);
        when(rentalMapper.toDto(pendingRental)).thenReturn(pendingRentalDto);
        when(rentalMapper.toDto(confirmedRental)).thenReturn(confirmedRentalDto);
        Page<RentalDto> expectedPage = new PageImpl<>(expectedDtos);

        Page<RentalDto> actual = rentalService
                .getActiveRentalByUserId(user.getId(), isActive, Pageable.unpaged());
        assertEquals(expectedPage.getContent(), actual.getContent());
        verify(rentalRepository).findByUserIdAndRentalStatusIn(user.getId(), activeRentals);
    }

    @Test
    @DisplayName("Should return a page of rentals "
            + "where rental status can be canceled or completed")
    void getActiveRentalByUserId_inactiveRentals_shouldReturnInactiveRentals() {
        User user = UserUtil.getExistedUser();
        boolean isActive = false;
        List<Rental.RentalStatus> inactiveRentals = List.of(
                Rental.RentalStatus.CANCELED,
                Rental.RentalStatus.COMPLETED);
        Rental pendingRental = RentalUtil.getCanceledRental();
        Rental confirmedRental = RentalUtil.getReturnedRental();
        List<Rental> rentalList = List.of(pendingRental, confirmedRental);
        List<RentalDto> expectedDtos = rentalList.stream()
                .map(rentalMapper::toDto)
                .toList();
        Page<RentalDto> expectedPage = new PageImpl<>(expectedDtos);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(rentalRepository.findByUserIdAndRentalStatusIn(user.getId(), inactiveRentals))
                .thenReturn(rentalList);

        Page<RentalDto> actual = rentalService
                .getActiveRentalByUserId(user.getId(), isActive, Pageable.unpaged());
        assertEquals(expectedPage.getContent(), actual.getContent());
        verify(rentalRepository).findByUserIdAndRentalStatusIn(user.getId(), inactiveRentals);
    }

    @Test
    @DisplayName("Should allow to delete rentals with pending status")
    void deleteRental_pendingRental_shouldSuccessfullyDeleteRental() {
        Rental rental = RentalUtil.getPendingRental();
        Payment payment = PaymentUtil.getPendingPayment();
        List<Payment> paymentsToDelete = List.of(payment);
        User user = UserUtil.getExistedUser();

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(paymentRepository
                .findAllByRentalIdAndPaymentStatus(rental.getId(), Payment.Status.PENDING))
                .thenReturn(paymentsToDelete);
        doNothing().when(paymentRepository).deleteAll(paymentsToDelete);
        when(carRepository.save(rental.getCar())).thenReturn(rental.getCar());
        doNothing().when(rentalRepository).delete(rental);
        doNothing().when(notificationService).sendNotification(anyString());

        assertDoesNotThrow(() -> rentalService.deleteRental(rental.getId(),
                        rental.getUser().getEmail()),
                "Should not throw an exception for a PENDING rental");

        verify(rentalRepository, times(2)).findById(rental.getId());
        verify(paymentRepository)
                .findAllByRentalIdAndPaymentStatus(rental.getId(), Payment.Status.PENDING);
        verify(paymentRepository)
                .deleteAll(paymentsToDelete);
        verify(carRepository).save(rental.getCar());
        verify(rentalRepository, times(1)).delete(rental);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("Should throw an error due to a rental status that is not eligible for deletion.")
    void deleteRental_confirmedRental_shouldThrowError() {
        Rental rental = RentalUtil.getConfirmedRental();
        String expectedMessage = "Rental by id: " + rental.getId()
                + " cannot be cancelled as it is: " + rental.getRentalStatus();
        User user = UserUtil.getExistedUser();

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        RentalException actual = assertThrows(RentalException.class,
                () -> rentalService.deleteRental(rental.getId(), rental.getUser().getEmail()));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should throw an error due to invalid rental id")
    void deleteRental_rentalNotFound_shouldThrowError() {
        Long rentalId = 99L;
        String expectedMessage = "Rental by rental id: " + rentalId + " not found";

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> rentalService.deleteRental(rentalId, anyString()));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should retrieve all rentals with PENDING or CONFIRM status")
    void getAllOverdueRentals_shouldReturnAllOverdueRentals() {
        Rental pendingRental = RentalUtil.getPendingRental();
        Rental confirmedRental = RentalUtil.getConfirmedRental();
        List<Rental> expected = List.of(pendingRental, confirmedRental);

        List<Rental.RentalStatus> rentalStatuses =
                List.of(Rental.RentalStatus.PENDING, Rental.RentalStatus.CONFIRMED);
        when(rentalRepository.findAllOverdueRentals(rentalStatuses)).thenReturn(expected);

        List<Rental> actual = rentalService.getAllOverdueRentals();
        verify(rentalRepository).findAllOverdueRentals(rentalStatuses);
        assertEquals(expected, actual);
    }
}
