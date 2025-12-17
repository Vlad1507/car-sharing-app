package com.app.carsharing.mapper;

import com.app.carsharing.config.MapperConfig;
import com.app.carsharing.dto.user.UpdateUserRequestDto;
import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import com.app.carsharing.model.Role;
import com.app.carsharing.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", source = "roles", qualifiedByName = "setRolesNames")
    UserDto toDto(User user);

    @Named("setRolesNames")
    default Set<String> setRolesNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());
    }

    User toModel(UserRegistrationRequestDto userDto);

    void updateUserFromDto(UpdateUserRequestDto updateRequestDto, @MappingTarget User user);
}
