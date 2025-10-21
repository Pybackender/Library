package com.example.bookmarket.service;

import com.example.bookmarket.dto.AddLibrarianDto;
import com.example.bookmarket.dto.UpdateLibrarianDto;
import com.example.bookmarket.entity.LibrarianEntity;
import com.example.bookmarket.exception.UserAlreadyLoggedInException;
import com.example.bookmarket.exception.UserAlreadyLoggedOutException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.LibrarianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibrarianService {
    private final LibrarianRepository librarianRepository;

    public LibrarianService(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    @Transactional
    public boolean add(AddLibrarianDto addLibrarianDto) {
        if (librarianRepository.existsByUsername(addLibrarianDto.username())) {
            return false; // If the username already exists, return false
        }

        var librarianEntity = new LibrarianEntity();
        librarianEntity.setUsername(addLibrarianDto.username());
        librarianEntity.setPassword(addLibrarianDto.password());

        librarianRepository.save(librarianEntity);
        return true; // Return true on successful addition
    }

    public boolean login(String username, String password) {
        LibrarianEntity librarian = librarianRepository.findByUsername(username);
        if (librarian == null) {
            throw new UserNotFoundException(username); // Throw exception if librarian not found
        }

        if (librarian.getStatus() == LibrarianEntity.LibrarianStatus.INACTIVE) {
            throw new UserNotFoundException("Librarian is inactive: " + username); // کاربر غیرفعال است
        }

        if (librarian.getStatus() == LibrarianEntity.LibrarianStatus.ACTIVE) {
            throw new UserAlreadyLoggedInException(username); // پرتاب استثنا در صورت ورود مجدد
        }

        // مقایسه رمز عبور
        return librarian.getPassword().equals(password);
    }

    @Transactional
    public boolean logout(Long id) {
        LibrarianEntity librarianEntity = librarianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // بررسی اینکه آیا کتابدار قبلاً غیرفعال شده است
        if (librarianEntity.getStatus() == LibrarianEntity.LibrarianStatus.INACTIVE) {
            throw new UserAlreadyLoggedOutException(librarianEntity.getUsername()); // پرتاب استثنا در صورت خروج مجدد
        }

        // تغییر وضعیت کتابدار به INACTIVE
        librarianEntity.setStatus(LibrarianEntity.LibrarianStatus.INACTIVE);
        librarianRepository.save(librarianEntity); // ذخیره تغییرات

        return true; // Return true on successful logout
    }

    @Transactional
    public boolean update(UpdateLibrarianDto updateLibrarianDto) {
        LibrarianEntity librarianEntity = librarianRepository.findById(updateLibrarianDto.id())
                .orElseThrow(() -> new UserNotFoundException(updateLibrarianDto.id())); // Throw exception if librarian not found

        if (updateLibrarianDto.username() != null) {
            librarianEntity.setUsername(updateLibrarianDto.username());
        }
        if (updateLibrarianDto.password() != null) {
            librarianEntity.setPassword(updateLibrarianDto.password());
        }

        librarianRepository.save(librarianEntity); // Save updated librarian
        return true; // Return true on successful update
    }

    @Transactional
    public boolean delete(Long id) {
        if (!librarianRepository.existsById(id)) {
            throw new UserNotFoundException(id); // Throw exception if librarian not found
        }
        librarianRepository.deleteById(id); // Delete the librarian
        return true; // Return true on successful deletion
    }
}