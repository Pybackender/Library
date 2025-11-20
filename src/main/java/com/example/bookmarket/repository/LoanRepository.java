package com.example.bookmarket.repository;

import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findByUser(UserEntity user);
    List<LoanEntity> findByBook(BookEntity book);
    List<LoanEntity> findByStatus(LoanStatus status);
    long countByStatus(LoanStatus status);
    long countByUserAndStatus(UserEntity user, LoanStatus status);
    List<LoanEntity> findByDueDateBeforeAndStatus(LocalDate dueDate, LoanStatus status);
}