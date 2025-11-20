package com.example.bookmarket.service;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.entity.LibrarianEntity;
import com.example.bookmarket.enums.LibrarianStatus;
import com.example.bookmarket.exception.*;
import com.example.bookmarket.repository.LibrarianRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class LibrarianService {
    private final LibrarianRepository librarianRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LibrarianService(LibrarianRepository librarianRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.librarianRepository = librarianRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AddLibrarianDto add(AddLibrarianDto addLibrarianDto) {
        if (librarianRepository.existsByUsername(addLibrarianDto.username())) {
            throw new RuntimeException("Librarian with username '" + addLibrarianDto.username() + "' already exists");
        }

        var librarianEntity = new LibrarianEntity();
        librarianEntity.setUsername(addLibrarianDto.username());
        librarianEntity.setPassword(passwordEncoder.encode(addLibrarianDto.password()));
        librarianEntity.setStatus(LibrarianStatus.INACTIVE); // تغییر به LibrarianStatus

        LibrarianEntity savedLibrarian = librarianRepository.save(librarianEntity);
        return convertToAddLibrarianDto(savedLibrarian);
    }

    public TokenDto login(String username, String password) {
        LibrarianEntity librarian = librarianRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (librarian.getStatus() == LibrarianStatus.ACTIVE) { // تغییر به LibrarianStatus
            throw new UserAlreadyLoggedInException(username);
        }

        if (!passwordEncoder.matches(password, librarian.getPassword())) {
            throw new IllegalArgumentException("Incorrect password"); // تغییر به InvalidPasswordException
        }

        librarian.setStatus(LibrarianStatus.ACTIVE); // تغییر به LibrarianStatus
        librarianRepository.save(librarian);

        Set<String> roles = getLibrarianRoles(username); // استفاده از متد getLibrarianRoles

        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new TokenDto(accessToken, refreshToken, "Login successful"); // اضافه کردن پیام
    }

    @Transactional
    public void logout(Long id) {
        LibrarianEntity librarianEntity = librarianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (librarianEntity.getStatus() == LibrarianStatus.INACTIVE) { // تغییر به LibrarianStatus
            throw new UserAlreadyLoggedOutException(librarianEntity.getUsername());
        }

        librarianEntity.setStatus(LibrarianStatus.INACTIVE); // تغییر به LibrarianStatus
        librarianRepository.save(librarianEntity);
    }

    @Transactional
    public UpdateLibrarianDto update(UpdateLibrarianDto updateLibrarianDto) {
        LibrarianEntity librarianEntity = librarianRepository.findById(updateLibrarianDto.id())
                .orElseThrow(() -> new UserNotFoundException(updateLibrarianDto.id()));

        if (updateLibrarianDto.username() != null) {
            librarianEntity.setUsername(updateLibrarianDto.username());
        }
        if (updateLibrarianDto.password() != null) {
            librarianEntity.setPassword(passwordEncoder.encode(updateLibrarianDto.password()));
        }

        LibrarianEntity updatedLibrarian = librarianRepository.save(librarianEntity);
        return convertToUpdateLibrarianDto(updatedLibrarian);
    }

    @Transactional
    public void delete(Long id) {
        if (!librarianRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        librarianRepository.deleteById(id);
    }

    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("Invalid token type. Refresh token required.");
            }

            if (!jwtUtil.validateToken(refreshToken, username)) {
                throw new InvalidTokenException("Refresh token is invalid or expired");
            }

            // بررسی وجود کتابدار در سیستم
            LibrarianEntity librarian = librarianRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            // بررسی وضعیت کتابدار
            if (librarian.getStatus() != LibrarianStatus.ACTIVE) { // تغییر به LibrarianStatus
                throw new UserInactiveException("Librarian account is not active");
            }

            Set<String> roles = getLibrarianRoles(username);
            String newAccessToken = jwtUtil.generateToken(username, roles);

            return new TokenDto(newAccessToken, refreshToken, "Access token refreshed successfully");

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            throw new TokenExpiredException("Refresh token is expired");
        } catch (Exception ex) {
            throw new InvalidTokenException("Refresh token is invalid");
        }
    }

    public Set<String> getLibrarianRoles(String username) {
        Optional<LibrarianEntity> librarian = librarianRepository.findByUsername(username);
        if (librarian.isPresent()) {
            // اگر در Entity نقش‌ها را دارید، از آن استفاده کنید
            // return librarian.get().getRoles();
            return Collections.singleton("ADMIN");
        }
        return Collections.emptySet();
    }

    // متدهای تبدیل Entity به DTO
    private AddLibrarianDto convertToAddLibrarianDto(LibrarianEntity librarian) {
        return new AddLibrarianDto(
                librarian.getUsername(),
                "" // پسورد رو برنمی‌گردونیم برای امنیت
        );
    }

    private UpdateLibrarianDto convertToUpdateLibrarianDto(LibrarianEntity librarian) {
        return new UpdateLibrarianDto(
                librarian.getId(),
                librarian.getUsername(),
                "" // پسورد رو برنمی‌گردونیم برای امنیت
        );
    }

    public LibrarianEntity findByUsername(String username) {
        return librarianRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}