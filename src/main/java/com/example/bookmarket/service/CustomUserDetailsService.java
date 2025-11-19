package com.example.bookmarket.service;

import com.example.bookmarket.entity.LibrarianEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.repository.LibrarianRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.bookmarket.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LibrarianRepository librarianRepository;

    public CustomUserDetailsService(UserRepository userRepository, LibrarianRepository librarianRepository) {
        this.userRepository = userRepository;
        this.librarianRepository = librarianRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ابتدا در librarian جستجو کنید
        Optional<LibrarianEntity> librarian = librarianRepository.findByUsername(username);
        if (librarian.isPresent()) {
            LibrarianEntity lib = librarian.get();

            // خواندن نقش‌ها از جدول librarian_roles
            List<GrantedAuthority> authorities = lib.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                    lib.getUsername(),
                    lib.getPassword(),
                    authorities // نقش‌های واقعی از دیتابیس
            );
        }

        // اگر librarian نبود، در user جستجو کنید
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            UserEntity userEntity = user.get();

            // خواندن نقش‌ها از جدول user_roles
            List<GrantedAuthority> authorities = userEntity.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                    userEntity.getUsername(),
                    userEntity.getPassword(),
                    authorities // نقش‌های واقعی از دیتابیس
            );
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}