package com.example.bookmarket.service;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.entity.LibrarianEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.LibrarianRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
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
    public boolean add(AddLibrarianDto addLibrarianDto) {
        if (librarianRepository.existsByUsername(addLibrarianDto.username())) {
            return false;
        }

        var librarianEntity = new LibrarianEntity();
        librarianEntity.setUsername(addLibrarianDto.username());
        librarianEntity.setPassword(passwordEncoder.encode(addLibrarianDto.password()));
        // status به طور خودکار INACTIVE تنظیم می‌شود
        // roles به طور خودکار ADMIN تنظیم می‌شود

        librarianRepository.save(librarianEntity);
        return true;
    }

    public TokenDto login(String username, String password) {
        LibrarianEntity librarian = librarianRepository.findByUsername(username);
        if (librarian == null) {
            throw new UserNotFoundException(username);
        }

        // بررسی وضعیت کتابدار - اگر null باشد یا INACTIVE باشد، اجازه login بده
        if (librarian.getStatus() == LibrarianEntity.LibrarianStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username);
        }

        // مقایسه رمز عبور
        if (!passwordEncoder.matches(password, librarian.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        // تغییر وضعیت به ACTIVE
        librarian.setStatus(LibrarianEntity.LibrarianStatus.ACTIVE);
        librarianRepository.save(librarian);

        // تخصیص نقش ADMIN به کتابدار
        Set<String> roles = Collections.singleton("ADMIN");

        // تولید توکن
        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new TokenDto(accessToken, refreshToken);
    }

    @Transactional
    public boolean logout(Long id) {
        LibrarianEntity librarianEntity = librarianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // بررسی اینکه آیا کتابدار قبلاً غیرفعال شده است
        if (librarianEntity.getStatus() == LibrarianEntity.LibrarianStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(librarianEntity.getUsername());
        }

        // تغییر وضعیت کتابدار به INACTIVE
        librarianEntity.setStatus(LibrarianEntity.LibrarianStatus.INACTIVE);
        librarianRepository.save(librarianEntity);

        return true;
    }

    @Transactional
    public boolean update(UpdateLibrarianDto updateLibrarianDto) {
        LibrarianEntity librarianEntity = librarianRepository.findById(updateLibrarianDto.id())
                .orElseThrow(() -> new UserNotFoundException(updateLibrarianDto.id()));

        if (updateLibrarianDto.username() != null) {
            librarianEntity.setUsername(updateLibrarianDto.username());
        }
        if (updateLibrarianDto.password() != null) {
            librarianEntity.setPassword(passwordEncoder.encode(updateLibrarianDto.password()));
        }

        librarianRepository.save(librarianEntity);
        return true;
    }

    @Transactional
    public boolean delete(Long id) {
        if (!librarianRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        librarianRepository.deleteById(id);
        return true;
    }

    public Set<String> getLibrarianRoles(String username) {
        LibrarianEntity librarian = librarianRepository.findByUsername(username);
        if (librarian != null) {
            return Collections.singleton("ADMIN"); // همیشه نقش ADMIN برگردانید
        }
        return Collections.emptySet();
    }
}