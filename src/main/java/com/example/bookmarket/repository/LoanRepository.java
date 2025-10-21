package com.example.bookmarket.repository;

import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findByUser(UserEntity user);
    List<LoanEntity> findByBook(BookEntity book);
    List<LoanEntity> findByStatus(LoanEntity.LoanStatus status);
    long countByStatus(LoanEntity.LoanStatus status);
    long countByUserAndStatus(UserEntity user, LoanEntity.LoanStatus status);


}
