package com.aurum.main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> genericExceptionHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Loop through all validation failures
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Return an HTTP 400 Bad Request status with the clean map
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOtpException(InvalidOtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRole(InvalidRoleException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadInvitationRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadInvitationRequest(BadInvitationRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInvitationTokenNotFound(InvitationTokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvitationTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleInvitationTokenExpired(InvitationTokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
