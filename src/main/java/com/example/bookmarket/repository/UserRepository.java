package com.example.bookmarket.repository;

import com.example.bookmarket.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    // استفاده از Optional برای جلوگیری از NullPointerException
    Optional<UserEntity> findByUsername(String username);
}