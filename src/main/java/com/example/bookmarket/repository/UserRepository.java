package com.example.bookmarket.repository;

import com.example.bookmarket.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByUsername(String name);
    UserEntity findByUsername(String username);

}

