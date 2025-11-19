package com.example.bookmarket.controller;

import com.example.bookmarket.dto.*;
import com.example.bookmarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.bookmarket.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Operation(summary = "ثبت نام کردن کاربر")
    @PostMapping("/register")
    public ResponseEntity<AddUserDto> add(@Valid @RequestBody AddUserDto addUserDto) {
        AddUserDto createdUser = userService.add(addUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "وارد شدن کاربر")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginUserDto loginRequestDto) {
        TokenDto tokenDto = userService.login(loginRequestDto.username(), loginRequestDto.password());
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "خارج شدن کاربر")
    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logout(@PathVariable Long userId) {
        userService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "بروزرسانی کردن کاربر")
    @PutMapping("/update")
    public ResponseEntity<UpdateUserDto> update(@Valid @RequestBody UpdateUserDto updateUserDto) {
        UpdateUserDto updatedUser = userService.update(updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "پاک کردن کاربر")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "گرفتن توکن جدید")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, null, "Invalid token type. Refresh token required."));
            }

            if (!jwtUtil.validateToken(refreshToken, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, null, "Refresh token is invalid or expired"));
            }

            String newAccessToken = jwtUtil.generateToken(username, userService.getUserRoles(username));
            logger.info("Roles for user {}: {}", username, userService.getUserRoles(username));

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
}