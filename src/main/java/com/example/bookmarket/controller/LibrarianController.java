package com.example.bookmarket.controller;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.RefreshTokenRequest;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.service.LibrarianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("api/v1/librarians")
@Tag(name = "مدیریت کتابدار ها", description = "")
public class LibrarianController {
    private final LibrarianService librarianService;
    private final JwtUtil jwtUtil;


    public LibrarianController(LibrarianService librarianService, JwtUtil jwtUtil) {
        this.librarianService = librarianService;
        this.jwtUtil = jwtUtil;
    }
    @Operation(summary = "ثبت نام کردن کتابدار ")
    @PostMapping("/register")
    public ResponseEntity<Boolean> add(@Valid @RequestBody AddLibrarianDto addLibrarianDto) {
        boolean createdLibrarian = librarianService.add(addLibrarianDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLibrarian);
    }
    @Operation(summary = "وارد شدن کتابدار ")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestParam String username, @RequestParam String password) {
        TokenDto tokenDto = librarianService.login(username, password); // استفاده از توکن DTO
        return ResponseEntity.ok(tokenDto); // برگرداندن توکن‌ها
    }
    @Operation(summary = "خارج شدن کتابدار ")
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        boolean loggedOut = librarianService.logout(userId);
        if (loggedOut) {
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or logout failed");
        }
    }
    @Operation(summary = "بروزرسانی کردن کتابدار ")
    @PutMapping("/update")
    public ResponseEntity<Boolean> update(@Valid @RequestBody UpdateLibrarianDto updateLibrarianDto) {
        boolean updatedLibrarian = librarianService.update(updateLibrarianDto);
        return ResponseEntity.ok(updatedLibrarian);
    }
    @Operation(summary = "پاک کردن کتابدار ")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = librarianService.delete(id);
        return ResponseEntity.ok(deleted);
    }
    @Operation(summary = "گرفتن توکن جدید")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            // استخراج نام کاربری از توکن
            String username = jwtUtil.extractUsername(refreshToken);
            // بررسی اعتبار توکن تجدید
            if (!jwtUtil.validateToken(refreshToken, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, "Refresh token is invalid or expired"));
            }
            // دریافت نقش‌های کتابدار
            Set<String> roles = librarianService.getLibrarianRoles(username);
            String newAccessToken = jwtUtil.generateToken(username, roles);
            // ساخت و بازگرداندن پاسخ
            TokenDto tokenDto = new TokenDto(newAccessToken, refreshToken);
            return ResponseEntity.ok(tokenDto);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenDto(null, "Refresh token is expired"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenDto(null, "Refresh token is invalid"));
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