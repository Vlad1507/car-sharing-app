package com.app.carsharing.mapper;

import com.app.carsharing.config.MapperConfig;
import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    Car toModel(CreateCarRequestDto carRequestDto);

    CarDto toDto(Car car);

    CarListDto toCarListDto(Car car);

    @Mapping(target = "availableCars", ignore = true)
    void updateCarFromDto(UpdateCarRequestDto updateCarRequestDto, @MappingTarget Car car);
}
