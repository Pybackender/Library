package com.example.bookmarket.service;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddUserDto;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateUserDto;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.enums.UserStatus;
import com.example.bookmarket.exception.*;
import com.example.bookmarket.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AddUserDto add(AddUserDto addUserDto) {
        if (userRepository.existsByUsername(addUserDto.username())) {
            throw new UsernameAlreadyExistsException(addUserDto.username());
        }

        var userEntity = new UserEntity();
        userEntity.setUsername(addUserDto.username());
        userEntity.setPassword(passwordEncoder.encode(addUserDto.password()));
        userEntity.setNickname(addUserDto.nickname());
        userEntity.setStatus(UserStatus.INACTIVE);

        userEntity.getRoles().add("USER");

        UserEntity savedUser = userRepository.save(userEntity);
        return convertToAddUserDto(savedUser);
    }

    public TokenDto login(String username, String password) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new IllegalArgumentException("رمزعبور اشتباه است");
        }

        if (userEntity.getStatus() == UserStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username);
        }

        if (userEntity.getStatus() == UserStatus.INACTIVE) {
            userEntity.setStatus(UserStatus.ACTIVE);
            userRepository.save(userEntity);
        }

        Set<String> roles = getUserRoles(username);

        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new TokenDto(accessToken, refreshToken, "Login successful");
    }

    @Transactional
    public UpdateUserDto update(UpdateUserDto updateUserDto) {
        Long userId = updateUserDto.userId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (updateUserDto.username() != null) {
            userEntity.setUsername(updateUserDto.username());
        }
        if (updateUserDto.password() != null) {
            userEntity.setPassword(passwordEncoder.encode(updateUserDto.password()));
        }
        if (updateUserDto.nickname() != null) {
            userEntity.setNickname(updateUserDto.nickname());
        }

        userEntity.setStatus(UserStatus.ACTIVE);
        UserEntity updatedUser = userRepository.save(userEntity);
        return convertToUpdateUserDto(updatedUser);
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void logout(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (userEntity.getStatus() == UserStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(userEntity.getUsername());
        }

        userEntity.setStatus(UserStatus.INACTIVE);
        userRepository.save(userEntity);
    }

    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        log.info("Attempting to refresh token");

        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                log.warn("Invalid token type used for refresh: {}", refreshToken);
                throw new InvalidTokenException("Invalid token type. Refresh token required.");
            }

            if (!jwtUtil.validateToken(refreshToken, username)) {
                log.warn("Refresh token validation failed for user: {}", username);
                throw new InvalidTokenException("Refresh token is invalid or expired");
            }

            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            if (user.getStatus() != UserStatus.ACTIVE) { // تغییر به UserStatus
                throw new UserInactiveException("User account is not active");
            }

            Set<String> roles = getUserRoles(username);
            log.debug("Roles for user {}: {}", username, roles);

            String newAccessToken = jwtUtil.generateToken(username, roles);

            log.info("Token refreshed successfully for user: {}", username);

            return new TokenDto(newAccessToken, refreshToken, "Access token refreshed successfully");

        } catch (ExpiredJwtException ex) {
            log.warn("Refresh token expired: {}", ex.getMessage());
            throw new TokenExpiredException("Refresh token is expired");
        } catch (UserNotFoundException | UserInactiveException ex) {
            log.warn("User issue during token refresh: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during token refresh: {}", ex.getMessage());
            throw new InvalidTokenException("Refresh token is invalid");
        }
    }

    public Set<String> getUserRoles(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            return user.getRoles() != null ? user.getRoles() : Collections.singleton("USER");
        }
        return Collections.singleton("USER");
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private AddUserDto convertToAddUserDto(UserEntity user) {
        return new AddUserDto(
                user.getUsername(),
                "",
                user.getNickname()
        );
    }

    private UpdateUserDto convertToUpdateUserDto(UserEntity user) {
        return new UpdateUserDto(
                user.getId(),
                user.getUsername(),
                "", // پسورد رو برنمی‌گردونیم برای امنیت
                user.getNickname()
        );
    }
}