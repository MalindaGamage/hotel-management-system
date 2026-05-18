package com.hotel.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// RFC 7807 ProblemDetail responses for all error conditions
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("https://hotel.com/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler({ConflictException.class, BookingConflictException.class})
    public ProblemDetail handleConflict(ConflictException ex, WebRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create("https://hotel.com/errors/conflict"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail handleIdempotency(IdempotencyConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Idempotency Conflict");
        pd.setType(URI.create("https://hotel.com/errors/idempotency-conflict"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Business Rule Violation");
        pd.setType(URI.create("https://hotel.com/errors/business-rule"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Invalid Token");
        pd.setType(URI.create("https://hotel.com/errors/invalid-token"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // Optimistic lock failure = concurrent booking detected
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "This record was modified by another request. Please retry.");
        pd.setTitle("Concurrent Modification");
        pd.setType(URI.create("https://hotel.com/errors/concurrent-modification"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
            "You do not have permission to perform this action");
        pd.setTitle("Access Denied");
        pd.setType(URI.create("https://hotel.com/errors/access-denied"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
            "Authentication failed: " + ex.getMessage());
        pd.setTitle("Authentication Failed");
        pd.setType(URI.create("https://hotel.com/errors/authentication"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Validation Error");
        pd.setType(URI.create("https://hotel.com/errors/validation"));
        pd.setProperty("fieldErrors", fieldErrors);
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest req) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("https://hotel.com/errors/internal"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
