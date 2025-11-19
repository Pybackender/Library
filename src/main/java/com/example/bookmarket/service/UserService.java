package com.example.bookmarket.service;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddUserDto;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateUserDto;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.exception.UsernameAlreadyExistsException;
import com.example.bookmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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
        userEntity.setStatus(UserEntity.UserStatus.INACTIVE);

        UserEntity savedUser = userRepository.save(userEntity);
        return convertToAddUserDto(savedUser);
    }

    public TokenDto login(String username, String password) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new IllegalArgumentException("رمزعبور اشتباه است");
        }

        if (userEntity.getStatus() == UserEntity.UserStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username);
        }

        if (userEntity.getStatus() == UserEntity.UserStatus.INACTIVE) {
            userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
            userRepository.save(userEntity);
        }

        Set<String> roles = Collections.singleton("USER");

        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        return new TokenDto(accessToken, refreshToken);
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

        userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
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

        if (userEntity.getStatus() == UserEntity.UserStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(userEntity.getUsername());
        }

        userEntity.setStatus(UserEntity.UserStatus.INACTIVE);
        userRepository.save(userEntity);
    }

    public Set<String> getUserRoles(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            return user.getRoles();
        }
        return Collections.emptySet();
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    // متدهای تبدیل Entity به DTO
    private AddUserDto convertToAddUserDto(UserEntity user) {
        return new AddUserDto(
                user.getUsername(),
                "", // پسورد رو برنمی‌گردونیم برای امنیت
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