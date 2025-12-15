package com.app.carsharing.mapper;

import com.app.carsharing.config.MapperConfig;
import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.model.Car;
import com.app.carsharing.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {

    @Mapping(source = "carId", target = "car", qualifiedByName = "setCar")
    Rental toModel(AddRentalRequestDto rentalRequestDto);

    @Named("setCar")
    default Car setCar(Long id) {
        Car car = new Car();
        car.setId(id);
        return car;
    }

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "car.id", target = "carId")
    })
    RentalDto toDto(Rental rental);

    @Mappings({
            @Mapping(source = "rental.user.id", target = "userId"),
            @Mapping(source = "rental.car.id", target = "carId"),
    })
    RentalDto toDtoWithPaymentUrl(Rental rental, String sessionUrl);
}
