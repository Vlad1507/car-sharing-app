package com.app.carsharing.controller;

import com.app.carsharing.dto.user.UpdateRoleRequestDto;
import com.app.carsharing.dto.user.UpdateUserRequestDto;
import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role of User",
            description = "Allows you to update the role for a user by their ID.")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable(name = "id") Long userId,
                                  @Valid @RequestBody UpdateRoleRequestDto updateRoleRequestDto,
                                  Authentication authentication) {
        UserDto response = userService.updateRole(userId, updateRoleRequestDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Return user information",
            description = "Allows you to return information about the user.")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        UserDto response = userService.getUser(authentication);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Update user information",
            description = "Allows you to update the profile information about the user")
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfileInfo(Authentication authentication,
                                     @Valid @RequestBody UpdateUserRequestDto updateRequestDto) {
        UserDto response = userService.updateUser(authentication, updateRequestDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
