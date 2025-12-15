package com.app.carsharing.dto.user;

import com.app.carsharing.validation.password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email
        @NotBlank
        String email,
        @Password
        @NotBlank
        String password) {
}
