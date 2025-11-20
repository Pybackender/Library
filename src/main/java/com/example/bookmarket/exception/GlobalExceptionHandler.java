package com.example.bookmarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    if ("rating".equals(error.getField())) {
                        return "rating : امتیاز باید بین ۱ تا ۵ باشد.";
                    }
                    return error.getField() + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        Map<String, String> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("message", errorMessage);
        response.put("status", HttpStatus.BAD_REQUEST.toString());

        return ResponseEntity.badRequest().body(response);
    }

    // Handler for BookNotFoundException
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookNotFound(BookNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handler for BookOutOfStockException
    @ExceptionHandler(BookOutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleBookOutOfStock(BookOutOfStockException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handler for UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handler for UsernameAlreadyExistsException
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handler for UserAlreadyLoggedInException
    @ExceptionHandler(UserAlreadyLoggedInException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyLoggedIn(UserAlreadyLoggedInException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handler for UserAlreadyLoggedOutException
    @ExceptionHandler(UserAlreadyLoggedOutException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyLoggedOut(UserAlreadyLoggedOutException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handler for LoanNotFoundException
    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLoanNotFound(LoanNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handler for LimitExceededException
    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleLimitExceeded(LimitExceededException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // Handler for BookAlreadyReturnedException
    @ExceptionHandler(BookAlreadyReturnedException.class)
    public ResponseEntity<Map<String, String>> handleBookAlreadyReturned(BookAlreadyReturnedException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handler for UserInactiveException
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<Map<String, String>> handleUserInactive(UserInactiveException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // Handler for CommentNotFoundException
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFound(CommentNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handler for generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return createErrorResponse("An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(TokenExpiredException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // Handler for InvalidTokenException
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // Helper method to create consistent error responses
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("status", status.toString());
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(response);
    }
}