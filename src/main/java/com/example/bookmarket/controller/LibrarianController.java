package com.example.bookmarket.controller;

import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.service.LibrarianService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/librarians")
public class LibrarianController {
    private final LibrarianService librarianService;

    public LibrarianController(LibrarianService librarianService) {
        this.librarianService = librarianService;
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> add(@Valid @RequestBody AddLibrarianDto addLibrarianDto) {
        boolean createdLibrarian = librarianService.add(addLibrarianDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLibrarian);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        boolean isAuthenticated = librarianService.login(username, password);
        if (isAuthenticated) {
            return ResponseEntity.ok("Librarian logged in successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        boolean loggedOut = librarianService.logout(userId);
        if (loggedOut) {
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or logout failed");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> update(@Valid @RequestBody UpdateLibrarianDto updateLibrarianDto) {
        boolean updatedLibrarian = librarianService.update(updateLibrarianDto);
        return ResponseEntity.ok(updatedLibrarian);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = librarianService.delete(id);
        return ResponseEntity.ok(deleted);
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