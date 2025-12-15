package com.app.carsharing.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = handleValidationException(ex, status);
        return ResponseEntity.status(status.value()).body(problemDetail);
    }

    private ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex, HttpStatusCode status) {
        String details = getErrorsDetails(ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, details);
        problemDetail.setType(URI.create("http://localhost:8080/errors/bad-request"));
        problemDetail.setTitle("Bad Request");
        problemDetail.setInstance(ex.getBody().getInstance());
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    private String getErrorsDetails(MethodArgumentNotValidException ex) {
        return Optional.of(ex.getDetailMessageArguments())
                .map(args -> Arrays.stream(args)
                        .filter(msg -> !ObjectUtils.isEmpty(msg))
                        .reduce("Please make sure to provide a valid request, ",
                                (a, b) -> a + " " + b)
                )
                .orElse("").toString();
    }

    @ExceptionHandler(RoleUpdateException.class)
    public ResponseEntity<Object> handleRoleUpdateException(
            RoleUpdateException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/bad-request"));
        problemDetail.setTitle("Role Update Failed");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(PaymentTypeException.class)
    public ResponseEntity<Object> handlePaymentTypeException(
            PaymentTypeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/bad-request"));
        problemDetail.setTitle("Payment Type Error");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(RentalException.class)
    public ResponseEntity<Object> handleRentalException(
            RentalException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/bad-request"));
        problemDetail.setTitle("Rental Failed");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(PaymentStatusException.class)
    public ResponseEntity<Object> handlePaymentStatusException(
            PaymentStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/bad-request"));
        problemDetail.setTitle("Payment Failed");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/not-found"));
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Object> handlePaymentNotFoundException(
            PaymentNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/not-found"));
        problemDetail.setTitle("Payment Not Found");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Object> handleRegistrationException(
            RegistrationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/conflict"));
        problemDetail.setTitle("Registration Failed");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(RentalAccessDeniedException.class)
    public ResponseEntity<Object> handleRentalAccessDeniedException(
            RentalAccessDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/forbidden"));
        problemDetail.setTitle("Rental Access Denied");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(PaymentAccessDeniedException.class)
    public ResponseEntity<Object> handlePaymentAccessDeniedException(
            PaymentAccessDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String requestUriEndpoint = request.getDescription(false);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create("http://localhost:8080/errors/forbidden"));
        problemDetail.setTitle("Payment Access Denied");
        problemDetail.setInstance(URI.create(requestUriEndpoint.replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problemDetail);
    }
}
