package com.app.carsharing.controller;

import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserLoginRequestDto;
import com.app.carsharing.dto.user.UserLoginResponseDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import com.app.carsharing.security.AuthenticationService;
import com.app.carsharing.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "User registration", security = @SecurityRequirement(name = "none"),
            description = "Register a new user if it does not exist already")
    @PostMapping("/registration")
    public ResponseEntity<UserDto> register(
            @RequestBody @Valid UserRegistrationRequestDto request) {
        UserDto response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "User login", security = @SecurityRequirement(name = "none"),
            description = "Authorizes the user who is logged in"
                    + " and provides them with a session key")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(
            @RequestBody @Valid UserLoginRequestDto request) {
        UserLoginResponseDto response = authenticationService.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
