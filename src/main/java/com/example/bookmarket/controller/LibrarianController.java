package com.example.bookmarket.controller;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.RefreshTokenRequest;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.service.LibrarianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "ثبت نام کردن کتابدار")
    @PostMapping("/register")
    public ResponseEntity<AddLibrarianDto> add(@Valid @RequestBody AddLibrarianDto addLibrarianDto) {
        AddLibrarianDto createdLibrarian = librarianService.add(addLibrarianDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLibrarian);
    }

    @Operation(summary = "وارد شدن کتابدار")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestParam String username, @RequestParam String password) {
        TokenDto tokenDto = librarianService.login(username, password);
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "خارج شدن کتابدار")
    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logout(@PathVariable Long userId) {
        librarianService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "بروزرسانی کردن کتابدار")
    @PutMapping("/update")
    public ResponseEntity<UpdateLibrarianDto> update(@Valid @RequestBody UpdateLibrarianDto updateLibrarianDto) {
        UpdateLibrarianDto updatedLibrarian = librarianService.update(updateLibrarianDto);
        return ResponseEntity.ok(updatedLibrarian);
    }

    @Operation(summary = "پاک کردن کتابدار")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        librarianService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "گرفتن توکن جدید")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            if (!jwtUtil.validateToken(refreshToken, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenDto(null, "Refresh token is invalid or expired"));
            }
            String newAccessToken = jwtUtil.generateToken(username, librarianService.getLibrarianRoles(username));
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
}