package com.example.bookmarket.service;

import com.example.bookmarket.dto.LoanDto;
import com.example.bookmarket.dto.UpdateLoanDto;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.enums.LoanStatus;
import com.example.bookmarket.enums.UserStatus;
import com.example.bookmarket.exception.*;
import com.example.bookmarket.repository.LoanRepository;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private LoanRepository loanRepository;
    private UserRepository userRepository;
    private BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public LoanDto createLoan(LoanDto loanDto) {
        UserEntity user = userRepository.findById(loanDto.userId())
                .orElseThrow(() -> new UserNotFoundException(loanDto.userId()));

        if (user.getStatus() == UserStatus.INACTIVE) { // تغییر به UserStatus
            throw new UserInactiveException(loanDto.userId());
        }

        BookEntity book = bookRepository.findById(loanDto.bookId())
                .orElseThrow(() -> new BookNotFoundException(loanDto.bookId()));

        long activeLoansCount = countActiveLoansByUserId(loanDto.userId());
        if (activeLoansCount >= 5) {
            throw new LimitExceededException(loanDto.userId());
        }

        Integer stock = book.getNumberOfBooks();
        if (stock == null || stock <= 0) {
            throw new BookOutOfStockException(loanDto.bookId());
        }

        book.setNumberOfBooks(stock - 1);
        bookRepository.save(book);

        LoanEntity loan = new LoanEntity();
        loan.setUser(user);
        loan.setBook(book);
        loan.setPrice(book.getPrice());
        loan.setFinalPrice(book.getFinalPrice());
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(loanDto.dueDate());

        LoanEntity savedLoan = loanRepository.save(loan);
        return convertToLoanDto(savedLoan);
    }

    @Transactional(readOnly = true)
    public Optional<UpdateLoanDto> findById(Long loanId) {
        return loanRepository.findById(loanId)
                .map(this::convertToUpdateLoanDto);
    }

    @Transactional(readOnly = true)
    public List<UpdateLoanDto> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::convertToUpdateLoanDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UpdateLoanDto updateLoan(UpdateLoanDto updateLoanDto) {
        LoanEntity loan = loanRepository.findById(updateLoanDto.id())
                .orElseThrow(() -> new LoanNotFoundException(updateLoanDto.id()));

        UserEntity user = userRepository.findById(updateLoanDto.userId())
                .orElseThrow(() -> new UserNotFoundException(updateLoanDto.userId()));

        if (user.getStatus() == UserStatus.INACTIVE) { // تغییر به UserStatus
            throw new UserInactiveException(updateLoanDto.userId());
        }

        long activeLoansCount = countActiveLoansByUserId(updateLoanDto.userId());
        if (activeLoansCount >= 5) {
            throw new LimitExceededException(updateLoanDto.userId());
        }

        BookEntity book = bookRepository.findById(updateLoanDto.bookId())
                .orElseThrow(() -> new BookNotFoundException(updateLoanDto.bookId()));

        loan.setUser(user);
        loan.setBook(book);
        loan.setDueDate(updateLoanDto.dueDate());
        loan.setPrice(book.getPrice());
        loan.setFinalPrice(book.getFinalPrice());

        LoanEntity updatedLoan = loanRepository.save(loan);
        return convertToUpdateLoanDto(updatedLoan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() == LoanStatus.ACTIVE) { // تغییر به LoanStatus
            BookEntity book = loan.getBook();
            Integer currentStock = book.getNumberOfBooks();
            book.setNumberOfBooks(currentStock + 1);
            bookRepository.save(book);
        }

        loanRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UpdateLoanDto> getLoansByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return loanRepository.findByUser(user).stream()
                .map(this::convertToUpdateLoanDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UpdateLoanDto> getLoansByBookId(Long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return loanRepository.findByBook(book).stream()
                .map(this::convertToUpdateLoanDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void returnLoan(Long loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) { // تغییر به LoanStatus
            throw new BookAlreadyReturnedException();
        }

        BookEntity book = loan.getBook();
        Integer currentStock = book.getNumberOfBooks();
        book.setNumberOfBooks(currentStock + 1);
        bookRepository.save(book);

        loan.setStatus(LoanStatus.RETURNED); // تغییر به LoanStatus
        loanRepository.save(loan);
    }

    public long countLoansByStatus(LoanStatus status) { // تغییر به LoanStatus
        return loanRepository.countByStatus(status);
    }

    public long countActiveLoans() {
        return countLoansByStatus(LoanStatus.ACTIVE); // تغییر به LoanStatus
    }

    public long countReturnedLoans() {
        return countLoansByStatus(LoanStatus.RETURNED); // تغییر به LoanStatus
    }

    public long countTotalLoans() {
        return countActiveLoans() + countReturnedLoans();
    }

    public List<UpdateLoanDto> getLoansByStatus(LoanStatus status) { // تغییر به LoanStatus
        return loanRepository.findByStatus(status).stream()
                .map(this::convertToUpdateLoanDto)
                .collect(Collectors.toList());
    }

    public long countActiveLoansByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return loanRepository.countByUserAndStatus(user, LoanStatus.ACTIVE); // تغییر به LoanStatus
    }

    // متدهای تبدیل Entity به DTO
    private LoanDto convertToLoanDto(LoanEntity loan) {
        return new LoanDto(
                loan.getUser().getId(),
                loan.getBook().getId(),
                loan.getDueDate()
        );
    }

    private UpdateLoanDto convertToUpdateLoanDto(LoanEntity loan) {
        return new UpdateLoanDto(
                loan.getId(),
                loan.getUser().getId(),
                loan.getBook().getId(),
                loan.getDueDate()
        );
    }

    @Transactional(readOnly = true)
    public List<UpdateLoanDto> searchLoans(Long userId, Long bookId, LoanStatus status) { // تغییر به LoanStatus
        if (userId != null) {
            // بررسی وجود کاربر
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            return getLoansByUserId(userId);
        } else if (bookId != null) {
            // بررسی وجود کتاب
            BookEntity book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new BookNotFoundException(bookId));
            return getLoansByBookId(bookId);
        } else if (status != null) {
            return getLoansByStatus(status);
        } else {
            return getAllLoans();
        }
    }
}