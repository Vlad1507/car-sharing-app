package com.app.carsharing.dto.user;

import com.app.carsharing.validation.match.FieldMatcher;
import com.app.carsharing.validation.password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@FieldMatcher(filed = "password", matchedField = "repeatPassword")
public record UserRegistrationRequestDto(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @Password
        @NotBlank
        String password,
        @NotBlank
        String repeatPassword
) {
}
