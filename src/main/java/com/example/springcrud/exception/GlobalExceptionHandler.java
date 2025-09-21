package com.example.springcrud.exception;

import com.example.springcrud.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // This annotation makes this class capable of handling exceptions across the entire application
public class GlobalExceptionHandler {

    // Constants for common map keys
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";



    // Handles validation errors specifically for @Valid and @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        // Iterate through all validation errors and collect them
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); // Get the name of the field that failed validation
            String errorMessage = error.getDefaultMessage();    // Get the message defined in the validation annotation
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ERROR, "Bad Request");
        body.put(MESSAGE, "Validation Failed");
        body.put(DETAILS, errors); // Include the map of field-specific errors

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // You can add more @ExceptionHandler methods here for other custom exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.NOT_FOUND.value());
        body.put(ERROR, "Not Found");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Generic exception handler for any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ERROR, "Internal Server Error");
        body.put(MESSAGE, ex.getMessage()); // Or a more generic message in production

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Object> handleInvalidException(InvalidInputException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value()); // Often a 400 Bad Request for invalid input
        body.put(ERROR, "Invalid Input");
        body.put(MESSAGE, ex.getMessage()); // The message from the thrown InvalidException

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserOperationException.class)
    public ResponseEntity<Object> handleUserOperationException(UserOperationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ERROR, "User Operation Failed");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiResponse response = ApiResponse.builder()
                .errorCode("ERR-101")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserIsPresentException.class)
    public ResponseEntity<Object> handle(UserIsPresentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.CONFLICT.value()); // 409 Conflict
        body.put(ERROR, "Conflict");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}
