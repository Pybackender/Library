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

        LibrarianEntity savedLibrarian = librarianRepository.save(librarianEntity);
        return convertToAddLibrarianDto(savedLibrarian);
    }

    public TokenDto login(String username, String password) {
        LibrarianEntity librarian = librarianRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (librarian.getStatus() == LibrarianEntity.LibrarianStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username);
        }

        if (!passwordEncoder.matches(password, librarian.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        librarian.setStatus(LibrarianEntity.LibrarianStatus.ACTIVE);
        librarianRepository.save(librarian);

        Set<String> roles = Collections.singleton("ADMIN");

        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new TokenDto(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long id) {
        LibrarianEntity librarianEntity = librarianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (librarianEntity.getStatus() == LibrarianEntity.LibrarianStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(librarianEntity.getUsername());
        }

        librarianEntity.setStatus(LibrarianEntity.LibrarianStatus.INACTIVE);
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

    public Set<String> getLibrarianRoles(String username) {
        Optional<LibrarianEntity> librarian = librarianRepository.findByUsername(username);
        if (librarian.isPresent()) {
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