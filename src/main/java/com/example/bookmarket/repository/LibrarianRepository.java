package com.example.bookmarket.repository;

import com.example.bookmarket.entity.LibrarianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibrarianRepository extends JpaRepository<LibrarianEntity, Long> {
    boolean existsByUsername(String username);
    Optional<LibrarianEntity> findByUsername(String username);
}