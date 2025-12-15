package com.app.carsharing.service.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.mapper.CarMapper;
import com.app.carsharing.model.Car;
import com.app.carsharing.repository.car.CarRepository;
import com.app.carsharing.util.CarUtil;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CarServiceTest {
    @MockitoBean
    private final CarRepository carRepository;
    @MockitoBean
    private final CarMapper carMapper;
    private final CarService carService;

    @Autowired
    public CarServiceTest(CarRepository carRepository,
                          CarMapper carMapper,
                          CarService carService) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carService = carService;
    }

    @Test
    @DisplayName("Should update the car with a positive number of cars")
    void updateCar_validCarPositiveQuantity_shouldUpdateCar() {
        UpdateCarRequestDto requestDto = CarUtil.getUpdateCarRequestDto();
        CarDto expected = new CarDto(
                1L,
                "Audi A5",
                "SEDAN",
                10,
                BigDecimal.valueOf(75.0)
        );
        Car car = CarUtil.getUpdatedCar(requestDto);

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateCarFromDto(requestDto, car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        CarDto actual = carService.updateCar(car.getId(), requestDto);
        assertEquals(expected, actual);
        verify(carMapper, times(1)).updateCarFromDto(requestDto, car);
    }

    @Test
    @DisplayName("Should throw an error due to wrong car id")
    void updateCar_invalidCarId_shouldThrowError() {
        UpdateCarRequestDto requestDto = CarUtil.getUpdateCarRequestDto();
        Car car = CarUtil.getUpdatedCar(requestDto);
        car.setId(999L);
        String expectedMessage = "Car with carId: " + car.getId() + " is not found";

        when(carRepository.findById(car.getId())).thenReturn(Optional.empty());

        Exception actual = assertThrows(EntityNotFoundException.class,
                () -> carService.updateCar(car.getId(), requestDto));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should delete car with valid id")
    void deleteCarById_validId_shouldReturnCar() {
        Long carId = 1L;

        when(carRepository.existsById(carId)).thenReturn(true);

        carService.deleteCarById(carId);
        verify(carRepository, times(1)).existsById(carId);
        verify(carRepository, times(1)).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Should throw an error due to invalid car id")
    void deleteCarById_invalidId_shouldThrowError() {
        Long carId = 99L;
        String expectedMessage = "Car with carId: " + carId + " is not found";

        when(carRepository.existsById(carId)).thenReturn(false);

        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> carService.deleteCarById(carId));
        assertEquals(expectedMessage, actual.getMessage());
    }
}
