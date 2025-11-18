package com.example.bookmarket.controller;

import com.example.bookmarket.dto.*;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.exception.UsernameAlreadyExistsException;
import com.example.bookmarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.example.bookmarket.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

@RestController
@RequestMapping("api/v1/user")
@Tag(name = "مدیریت کاربران", description = "")
@Validated
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "ثبت نام کردن کاربر ")
    @PostMapping("/register")
    public ResponseEntity<UserEntity> add(@Valid @RequestBody AddUserDto addUserDto) {
        UserEntity createdUser = userService.add(addUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "وارد شدن کاربر ")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginUserDto loginRequestDto) {
        TokenDto tokenDto = userService.login(loginRequestDto.username(), loginRequestDto.password());

        // بازگرداندن پاسخ با توکن‌ها
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "خارج شدن کاربر ")
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        boolean loggedOut = userService.logout(userId);
        if (loggedOut) {
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or logout failed");
        }
    }

    @Operation(summary = "بروزرسانی کردن کاربر ")
    @PutMapping("/update")
    public ResponseEntity<String> update(@Valid @RequestBody UpdateUserDto updateUserDto) {
        boolean updatedUser = userService.update(updateUserDto);
        if (updatedUser) {
            return ResponseEntity.ok("User update successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User did not update successfully");
        }
    }

    @Operation(summary = "پاک کردن کاربر ")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Boolean> delete(@PathVariable Long userId) {
        boolean deleted = userService.delete(userId);
        if (deleted) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @Operation(summary = "گرفتن توکن جدید")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // استخراج نام کاربری از توکن
            String username = jwtUtil.extractUsername(refreshToken);

            // بررسی اینکه توکن ارسالی واقعاً یک refresh token است
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, null, "Invalid token type. Refresh token required."));
            }

            // بررسی اعتبار توکن تجدید
            if (!jwtUtil.validateToken(refreshToken, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, null, "Refresh token is invalid or expired"));
            }

            // دریافت نقش‌های کاربر از دیتابیس
            Set<String> roles = userService.getUserRoles(username);
            logger.info("Roles for user {}: {}", username, roles);

            // تولید فقط توکن دسترسی جدید با نقش‌ها
            String newAccessToken = jwtUtil.generateToken(username, roles);

            // ساخت و بازگرداندن پاسخ با refresh token اصلی
            TokenDto tokenDto = new TokenDto(newAccessToken, refreshToken, "Access token refreshed successfully");
            return ResponseEntity.ok(tokenDto);

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenDto(null, null, "Refresh token is expired"));
        } catch (Exception ex) {
            logger.error("Error refreshing token: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenDto(null, null, "Refresh token is invalid"));
        }
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<String> handleProductNotFound(UsernameAlreadyExistsException ex) {
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
