package com.example.bookmarket.service;

import com.example.bookmarket.config.JwtUtil;
import com.example.bookmarket.dto.AddUserDto;
import com.example.bookmarket.dto.TokenDto;
import com.example.bookmarket.dto.UpdateUserDto;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder; // اضافه کردن PasswordEncoder

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder; // تزریق PasswordEncoder
    }

    @Transactional
    public boolean add(AddUserDto addUserDto) {
        if (userRepository.existsByUsername(addUserDto.username())) {
            return false;
        }

        var userEntity = new UserEntity();
        userEntity.setUsername(addUserDto.username());
        userEntity.setPassword(passwordEncoder.encode(addUserDto.password())); // رمزنگاری رمز عبور
        userEntity.setNickname(addUserDto.nickname());
        userEntity.setStatus(UserEntity.UserStatus.INACTIVE); // وضعیت جدید برای کاربر ثبت نام شده

        userRepository.save(userEntity);
        return true;
    }

    public TokenDto login(String username, String password) {
        // جستجوی کاربر در پایگاه داده
        UserEntity userEntity = userRepository.findByUsername(username);

        // بررسی وجود کاربر
        if (userEntity == null) {
            throw new UserNotFoundException(username);
        }

        // بررسی رمز عبور با استفاده از PasswordEncoder
        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        // بررسی وضعیت کاربر (آیا کاربر قبلاً وارد شده است)
        if (userEntity.getStatus() == UserEntity.UserStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username);
        }

        // اگر کاربر INACTIVE باشد، وضعیت او را به ACTIVE تغییر دهید
        if (userEntity.getStatus() == UserEntity.UserStatus.INACTIVE) {
            userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
            userRepository.save(userEntity); // ذخیره تغییرات در پایگاه داده
        }

        // تخصیص نقش USER به کاربر
        Set<String> roles = Collections.singleton("USER");

        // تولید توکن
        String accessToken = jwtUtil.generateToken(username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // برگرداندن توکن‌ها در قالب TokenDto
        return new TokenDto(accessToken, refreshToken);
    }

    @Transactional
    public boolean update(UpdateUserDto updateUserDto) {
        Long userId = updateUserDto.userId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (updateUserDto.username() != null) {
            userEntity.setUsername(updateUserDto.username());
        }
        if (updateUserDto.password() != null) {
            userEntity.setPassword(passwordEncoder.encode(updateUserDto.password())); // رمزنگاری رمز عبور جدید
        }
        if (updateUserDto.nickname() != null) {
            userEntity.setNickname(updateUserDto.nickname());
        }

        userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        userRepository.save(userEntity);
        return true;
    }

    @Transactional
    public boolean delete(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        } else {
            throw new UserNotFoundException(userId);
        }
    }

    @Transactional
    public boolean logout(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (userEntity.getStatus() == UserEntity.UserStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(userEntity.getUsername());
        }

        userEntity.setStatus(UserEntity.UserStatus.INACTIVE);
        userRepository.save(userEntity);

        return true; // خروج موفق
    }

    public Set<String> getUserRoles(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user != null) {
            return user.getRoles();
        }
        return Collections.emptySet();
    }
}