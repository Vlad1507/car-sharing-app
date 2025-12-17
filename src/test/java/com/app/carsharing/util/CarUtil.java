package com.app.carsharing.util;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.model.Car;
import java.math.BigDecimal;

public class CarUtil {

    public static CarDto getUpdatedCarDto() {
        return new CarDto(
                1L,
                "Audi A5",
                "SEDAN",
                6,
                BigDecimal.valueOf(35.0)
        );
    }

    public static Car getUpdatedCar(UpdateCarRequestDto updateCarRequestDto) {
        Car car = new Car();
        car.setId(1L);
        car.setModel(updateCarRequestDto.model());
        car.setBodyType(Car.BodyType.valueOf(updateCarRequestDto.bodyType()));
        car.setAvailableCars(updateCarRequestDto.quantityToAdd());
        car.setDailyFee(updateCarRequestDto.dailyFee());
        return car;
    }

    public static Car getExistedCar() {
        Car car = new Car();
        car.setId(1L);
        car.setModel("Audi A5");
        car.setBodyType(Car.BodyType.valueOf("SEDAN"));
        car.setAvailableCars(1);
        car.setDailyFee(BigDecimal.valueOf(35.0));
        return car;
    }

    public static Car getSecondExistedCar() {
        Car car = new Car();
        car.setId(2L);
        car.setModel("BMW X3");
        car.setBodyType(Car.BodyType.valueOf("SUV"));
        car.setAvailableCars(1);
        car.setDailyFee(BigDecimal.valueOf(45.0));
        return car;
    }

    public static UpdateCarRequestDto getUpdateCarRequestDto() {
        return new UpdateCarRequestDto(
                "Audi A5",
                "SEDAN",
                5,
                BigDecimal.valueOf(35.0)
        );
    }

    public static Car getNoAvailableCar() {
        Car car = new Car();
        car.setId(1L);
        car.setModel("Audi A5");
        car.setBodyType(Car.BodyType.valueOf("SEDAN"));
        car.setAvailableCars(0);
        car.setDailyFee(BigDecimal.valueOf(35.0));
        return car;
    }

    public static CreateCarRequestDto getCreateCarDto() {
        return new CreateCarRequestDto(
                "Audi A5",
                "SEDAN",
                BigDecimal.valueOf(35.0),
                1
        );
    }

    public static CarDto getCarDto() {
        return new CarDto(
                1L,
                "Audi A5",
                "SEDAN",
                1,
                BigDecimal.valueOf(35.00)
        );
    }

    public static CreateCarRequestDto getCreateCarInvalidQuantityDto() {
        return new CreateCarRequestDto(
                "Audi A5",
                "SEDAN",
                BigDecimal.valueOf(35.0),
                -3
        );
    }

    public static CarListDto getCarListDto() {
        return new CarListDto(
                1L,
                "Audi A5",
                BigDecimal.valueOf(35.0)
        );
    }
}
