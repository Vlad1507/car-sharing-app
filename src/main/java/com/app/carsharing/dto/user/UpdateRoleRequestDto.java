package com.app.carsharing.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequestDto(@NotBlank String role) {
}
