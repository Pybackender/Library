package com.example.bookmarket.service;

import com.example.bookmarket.repository.BookRepository;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.LoanRepository;
import com.example.bookmarket.dto.StatisticsDto;
import com.example.bookmarket.entity.LoanEntity; // اطمینان حاصل کنید که این import وجود دارد
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public StatisticsService(BookRepository bookRepository, UserRepository userRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    public StatisticsDto getStatistics() {
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalLoans = loanRepository.count();
        long activeLoans = loanRepository.countByStatus(LoanEntity.LoanStatus.ACTIVE);
        long returnedLoans = loanRepository.countByStatus(LoanEntity.LoanStatus.RETURNED);

        // ایجاد یک شیء جدید از StatisticsDto
        return new StatisticsDto(totalBooks, totalUsers, totalLoans, activeLoans, returnedLoans);
    }
}