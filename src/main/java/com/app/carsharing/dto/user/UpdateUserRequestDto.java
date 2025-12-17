package com.app.carsharing.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDto(
        @NotBlank String firstName,
        @NotBlank String lastName) {
}
