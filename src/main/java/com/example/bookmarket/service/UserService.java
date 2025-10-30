package com.example.bookmarket.service;


import com.example.bookmarket.dto.AddUserDto;
import com.example.bookmarket.dto.UpdateUserDto;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean add(AddUserDto addUserDto) {
        if (userRepository.existsByUsername(addUserDto.username())) {
            return false;
        }

        var userEntity = new UserEntity();
        userEntity.setUsername(addUserDto.username());
        userEntity.setPassword(addUserDto.password());
        userEntity.setNickname(addUserDto.nickname());
        userEntity.setStatus(UserEntity.UserStatus.ACTIVE); // وضعیت کاربر را ACTIVE تنظیم کنید

        userRepository.save(userEntity);
        return true;
    }

    public boolean login(String username, String password) {
        UserEntity userEntity = userRepository.findByUsername(username);

        // اگر کاربر وجود ندارد، یک استثنا پرتاب کنید
        if (userEntity == null) {
            throw new UserNotFoundException(username);
        }

        // اگر کاربر قبلاً فعال است
        if (userEntity.getStatus() == UserEntity.UserStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username); // پرتاب استثنا در صورت ورود مجدد
        }

        // مقایسه رمز عبور
        if (userEntity.getPassword().equals(password)) {
            userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
            userRepository.save(userEntity);
            return true; // ورود موفق
        }
        return false; // رمز عبور نادرست
    }

    @Transactional
    public boolean update(UpdateUserDto updateUserDto) {
        Long userId = updateUserDto.userId();
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // به روزرسانی فیلدها به صورت لازم
        if (updateUserDto.username() != null) {
            userEntity.setUsername(updateUserDto.username());
        }
        if (updateUserDto.password() != null) {
            userEntity.setPassword(updateUserDto.password());
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

        // اگر کاربر قبلاً غیرفعال است
        if (userEntity.getStatus() == UserEntity.UserStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(userEntity.getUsername()); // پرتاب استثنا در صورت خروج مجدد
        }

        // وضعیت کاربر را به INACTIVE تغییر دهید
        userEntity.setStatus(UserEntity.UserStatus.INACTIVE);
        userRepository.save(userEntity);

        return true; // خروج موفق
    }
}