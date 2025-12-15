package com.app.carsharing.exception;

public class RentalAccessDeniedException extends RuntimeException {
    public RentalAccessDeniedException(String message) {
        super(message);
    }
}
