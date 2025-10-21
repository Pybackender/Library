package com.example.bookmarket.repository;

import com.example.bookmarket.entity.LibrarianEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibrarianRepository extends JpaRepository<LibrarianEntity, Long> {
    boolean existsByUsername(String username);
    LibrarianEntity findByUsername(String username);
}