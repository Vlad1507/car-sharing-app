package com.app.carsharing.exception;

public class InvalidRentalDurationException extends RuntimeException {
    public InvalidRentalDurationException(String message) {
        super(message);
    }
}
