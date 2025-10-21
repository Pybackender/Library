package com.example.bookmarket.service;

import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.repository.LoanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class LoanNotificationService {
    private final LoanRepository loanRepository;

    public LoanNotificationService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Scheduled(fixedRate = 86400000) // هر 24 ساعت یک بار
    public void checkDueLoans() {
        List<LoanEntity> loans = loanRepository.findAll(); // همه قرض‌ها را دریافت کنید
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault()); // زمان فعلی با منطقه زمانی

        for (LoanEntity loan : loans) {
            LocalDateTime dueDate = loan.getDueDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            if (dueDate.isBefore(now) && loan.getStatus() == LoanEntity.LoanStatus.ACTIVE) {
                sendNotification(loan, dueDate, now);
            }
        }
    }

    private void sendNotification(LoanEntity loan, LocalDateTime dueDate, LocalDateTime now) {
        // محاسبه زمان گذشته از تاریخ سررسید
        Duration duration = Duration.between(dueDate, now);
        long hours = duration.toHours();
        long days = duration.toDays();

        // نمایش اعلان به کاربر
        System.out.println("Loan overdue! User: " + loan.getUser().getUsername() +
                ", Book: " + loan.getBook().getTitle() +
                ", Overdue by: " + days + " days and " + (hours % 24) + " hours");

    }
}