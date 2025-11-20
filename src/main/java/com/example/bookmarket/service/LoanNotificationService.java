package com.example.bookmarket.service;

import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.enums.LoanStatus;
import com.example.bookmarket.repository.LoanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanNotificationService {
    private final LoanRepository loanRepository;

    public LoanNotificationService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Scheduled(fixedRate = 86400000) // هر 24 ساعت یک بار
    public void checkDueLoans() {
        LocalDate today = LocalDate.now();

        // فقط وام‌های overdue را دریافت کنید
        List<LoanEntity> overdueLoans = loanRepository
                .findByDueDateBeforeAndStatus(today, LoanStatus.ACTIVE);

        for (LoanEntity loan : overdueLoans) {
            sendNotification(loan, loan.getDueDate(), today);
        }
    }

    private void sendNotification(LoanEntity loan, LocalDate dueDate, LocalDate now) {
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);

        System.out.println("Loan overdue! User: " + loan.getUser().getUsername() +
                ", Book: " + loan.getBook().getTitle() +
                ", Overdue by: " + daysOverdue + " days");

        // در اینجا می‌توانید ایمیل، پیامک یا نوتیفیکیشن واقعی ارسال کنید
    }
}