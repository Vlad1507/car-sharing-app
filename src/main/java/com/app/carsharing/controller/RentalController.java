package com.app.carsharing.controller;

import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create rental",
            description = "Allows you to rent a car if one is available")
    @PostMapping
    public ResponseEntity<RentalDto> addRental(
            @RequestBody @Valid AddRentalRequestDto rentalRequestDto,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        RentalDto response = rentalService.addRental(userEmail, rentalRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') "
            + "OR (hasRole('CUSTOMER') AND #userId == principal.id)")
    @Operation(summary = "Get current or ended rentals",
            description = "Returns rentals by user ID and information about"
                    + " whether they are active or not")
    @GetMapping
    public Page<RentalDto> getRentalsByUserIdAndActivityStatus(
            @RequestParam Long userId,
            @RequestParam boolean isActive,
            Pageable pageable) {
        Page<RentalDto> response = rentalService
                .getActiveRentalByUserId(userId, isActive, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK).getBody();
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Receive rental by id",
            description = "Return rental by rental id")
    @GetMapping("/{id}")
    public ResponseEntity<RentalDto> getRentalById(@PathVariable Long id) {
        RentalDto response = rentalService.findRentalById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @Operation(summary = "End of rent",
            description = "Allows to finish the rent, sets an actual return date,"
                    + " and triggers a fine check")
    @PostMapping("/{rentalId}/return")
    public ResponseEntity<RentalDto> endRental(@PathVariable Long rentalId) {
        LocalDate actualReturnDate = LocalDate.now();
        RentalDto response = rentalService.endRental(rentalId, actualReturnDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Cancel pending operation",
            description = "Allows to delete chosen rental")
    @DeleteMapping("/{rentalId}")
    public ResponseEntity<Void> cancelPendingRental(
            @PathVariable Long rentalId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        rentalService.deleteRental(rentalId, userEmail);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
