package com.app.carsharing.mapper;

import com.app.carsharing.config.MapperConfig;
import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.model.Payment;
import com.app.carsharing.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(target = "rental", source = "rentalId", qualifiedByName = "setRental")
    @Mapping(target = "paymentType", ignore = true)
    Payment toModel(CreatePaymentRequestSessionDto requestSessionDto);

    @Named("setRental")
    default Rental setRental(Long id) {
        Rental rental = new Rental();
        rental.setId(id);
        return rental;
    }

    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto toDto(Payment payment);
}
