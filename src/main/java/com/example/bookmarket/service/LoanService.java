package com.example.bookmarket.service;

import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.*;
import com.example.bookmarket.repository.LoanRepository;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public LoanEntity createLoan(Long userId, Long bookId, Date dueDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // بررسی وضعیت کاربر
        if (user.getStatus() == UserEntity.UserStatus.INACTIVE) {
            throw new UserInactiveException(userId); // پرتاب استثنا در صورت غیرفعالی کاربر
        }

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        // بررسی تعداد وام‌های فعال
        long activeLoansCount = countActiveLoansByUserId(userId);
        if (activeLoansCount >= 5) {
            throw new LimitExceededException(userId); // استثنای سفارشی ایجاد کنید
        }

        Integer stock = book.getNumberOfBooks();
        if (stock == null || stock <= 0) {
            throw new BookOutOfStockException(bookId);
        }

        // کاهش stock کتاب
        book.setNumberOfBooks(stock - 1);
        bookRepository.save(book);

        // ایجاد loan
        LoanEntity loan = new LoanEntity();
        loan.setUser(user);
        loan.setBook(book);
        loan.setPrice(book.getPrice());
        loan.setFinalPrice(book.getFinalPrice());
        loan.setLoanDate(new Date());
        loan.setDueDate(dueDate);

        return loanRepository.save(loan);
    }

    @Transactional
    public Optional<LoanEntity> findById(Long loanId) {
        return loanRepository.findById(loanId);
    }

    public List<LoanEntity> getAllLoans() {
        return loanRepository.findAll();
    }

    public LoanEntity updateLoan(Long id, Long userId, Long bookId, Date dueDate) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        // بررسی وضعیت کاربر
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // بررسی وضعیت کاربر
        if (user.getStatus() == UserEntity.UserStatus.INACTIVE) {
            throw new UserInactiveException(userId); // پرتاب استثنا در صورت غیرفعالی کاربر
        }

        // بررسی تعداد وام‌های فعال
        long activeLoansCount = countActiveLoansByUserId(userId);
        if (activeLoansCount >= 5) {
            throw new LimitExceededException(userId); // استثنای سفارشی ایجاد کنید
        }

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        loan.setUser(user);
        loan.setBook(book);
        loan.setDueDate(dueDate);
        loan.setPrice(book.getPrice());
        loan.setFinalPrice(book.getFinalPrice());

        return loanRepository.save(loan);
    }

    public void deleteLoan(Long id) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id)); // اگر وام وجود نداشته باشد، استثنا پرتاب می‌شود

        if (loan.getStatus() == LoanEntity.LoanStatus.ACTIVE) {
            // اگر وام فعال است، کتاب را به موجودی اضافه کنید
            BookEntity book = loan.getBook();
            Integer currentStock = book.getNumberOfBooks();
            book.setNumberOfBooks(currentStock + 1);
            bookRepository.save(book);
        }

        loanRepository.deleteById(id);
    }

    public List<LoanEntity> getLoansByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return loanRepository.findByUser(user);
    }

    public List<LoanEntity> getLoansByBookId(Long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return loanRepository.findByBook(book);
    }

    @Transactional
    public void returnLoan(Long loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        if (loan.getStatus() == LoanEntity.LoanStatus.RETURNED) {
            throw new BookAlreadyReturnedException(); // می‌توانید استثنا بسازید
        }

        BookEntity book = loan.getBook();
        Integer currentStock = book.getNumberOfBooks();
        book.setNumberOfBooks(currentStock + 1);
        bookRepository.save(book);

        loan.setStatus(LoanEntity.LoanStatus.RETURNED); // به روز کردن وضعیت به RETURNED
        loanRepository.save(loan);
    }

    public long countLoansByStatus(LoanEntity.LoanStatus status) {
        return loanRepository.countByStatus(status);
    }

    public long countActiveLoans() {
        return countLoansByStatus(LoanEntity.LoanStatus.ACTIVE);
    }

    public long countReturnedLoans() {
        return countLoansByStatus(LoanEntity.LoanStatus.RETURNED);
    }

    public long countTotalLoans() {
        return countActiveLoans() + countReturnedLoans();
    }

    public List<LoanEntity> getLoansByStatus(LoanEntity.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    public long countActiveLoansByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return loanRepository.countByUserAndStatus(user, LoanEntity.LoanStatus.ACTIVE);
    }
}
