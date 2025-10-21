package com.example.bookmarket.controller;

import com.example.bookmarket.dto.AddUserDto;
import com.example.bookmarket.dto.UpdateUserDto;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> add(@Valid @RequestBody AddUserDto addUserDto) {
        boolean createdUser = userService.add(addUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        boolean isAuthenticated = userService.login(username, password);
        if (isAuthenticated) {
            return ResponseEntity.ok("User logged in successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        boolean loggedOut = userService.logout(userId);
        if (loggedOut) {
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or logout failed");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> update(@Valid @RequestBody UpdateUserDto updateUserDto) {
        boolean updatedUser = userService.update(updateUserDto);
        if (updatedUser) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Boolean> delete(@PathVariable Long userId) {
        boolean deleted = userService.delete(userId);
        if (deleted) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyLoggedInException.class)
    public ResponseEntity<String> handleProductNotFound(UserAlreadyLoggedInException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyLoggedOutException.class)
    public ResponseEntity<String> handleProductNotFound(UserAlreadyLoggedOutException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Exception handler for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + ", " + second)
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
