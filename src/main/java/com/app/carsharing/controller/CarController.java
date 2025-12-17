package com.app.carsharing.controller;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car management", description = "Endpoints for managing cars")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Add new car",
            description = "Allows to add new car by required parameters")
    @PostMapping
    public ResponseEntity<CarDto> addCar(@RequestBody @Valid CreateCarRequestDto carRequestDto) {
        CarDto response = carService.addCar(carRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Receive all cars",
            description = "Allows to return all cars in page presentation")
    @GetMapping
    public Page<CarListDto> getAllCars(Pageable pageable) {
        return carService.findAllCars(pageable);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Return car by id",
            description = "Return information about the car by the ID")
    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getCarInfo(@PathVariable(name = "id") Long carId) {
        CarDto response = carService.findCarById(carId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Update car by id",
            description = "Allows to update information about the car by ID")
    @PatchMapping("/{id}")
    public ResponseEntity<CarDto> updateCarById(@PathVariable(name = "id") Long carId,
                                @RequestBody @Valid UpdateCarRequestDto requestDto) {
        CarDto response = carService.updateCar(carId, requestDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Delete car by id",
            description = "Allows to remove car by ID if exist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable(name = "id") Long carId) {
        carService.deleteCarById(carId);
    }
}
