package com.app.carsharing.service.car;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarDto addCar(CreateCarRequestDto carRequestDto);

    Page<CarListDto> findAllCars(Pageable pageable);

    CarDto findCarById(Long id);

    CarDto updateCar(Long id, UpdateCarRequestDto updateCarRequestDto);

    void deleteCarById(Long id);

}
