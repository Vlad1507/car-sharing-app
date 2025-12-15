package com.app.carsharing.service.user;

import com.app.carsharing.dto.user.UpdateRoleRequestDto;
import com.app.carsharing.dto.user.UpdateUserRequestDto;
import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import org.springframework.security.core.Authentication;

public interface UserService {

    UserDto register(UserRegistrationRequestDto request);

    UserDto updateRole(Long userId,
                       UpdateRoleRequestDto updateRoleRequestDto);

    UserDto getUser(Authentication authentication);

    UserDto updateUser(Authentication authentication, UpdateUserRequestDto updateRequestDto);
}
