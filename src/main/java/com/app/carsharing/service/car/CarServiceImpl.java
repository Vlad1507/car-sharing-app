package com.app.carsharing.service.car;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.mapper.CarMapper;
import com.app.carsharing.model.Car;
import com.app.carsharing.repository.car.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto addCar(CreateCarRequestDto carRequestDto) {
        Car car = carMapper.toModel(carRequestDto);
        car.setAvailableCars(carRequestDto.quantity());
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public Page<CarListDto> findAllCars(Pageable pageable) {
        return carRepository.findAll(pageable).map(carMapper::toCarListDto);
    }

    @Override
    public CarDto findCarById(Long id) {
        Car car = getCarById(id);
        return carMapper.toDto(car);
    }

    @Override
    public CarDto updateCar(Long id, UpdateCarRequestDto updateCarRequestDto) {
        Car car = getCarById(id);
        Integer sum = car.getAvailableCars() + updateCarRequestDto.quantityToAdd();
        carMapper.updateCarFromDto(updateCarRequestDto, car);
        car.setAvailableCars(sum);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public void deleteCarById(Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Car with carId: " + id + " is not found");
        }
    }

    private Car getCarById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car with carId: " + id + " is not found")
        );
    }
}
