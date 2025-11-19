package com.example.bookmarket.service;

import com.example.bookmarket.dto.LoanDto;
import com.example.bookmarket.dto.UpdateLoanDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public LoanDto createLoan(LoanDto loanDto) {
        UserEntity user = userRepository.findById(loanDto.userId())
                .orElseThrow(() -> new UserNotFoundException(loanDto.userId()));

        if (user.getStatus() == UserEntity.UserStatus.INACTIVE) {
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

        if (user.getStatus() == UserEntity.UserStatus.INACTIVE) {
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

        if (loan.getStatus() == LoanEntity.LoanStatus.ACTIVE) {
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

        if (loan.getStatus() == LoanEntity.LoanStatus.RETURNED) {
            throw new BookAlreadyReturnedException();
        }

        BookEntity book = loan.getBook();
        Integer currentStock = book.getNumberOfBooks();
        book.setNumberOfBooks(currentStock + 1);
        bookRepository.save(book);

        loan.setStatus(LoanEntity.LoanStatus.RETURNED);
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

    public List<UpdateLoanDto> getLoansByStatus(LoanEntity.LoanStatus status) {
        return loanRepository.findByStatus(status).stream()
                .map(this::convertToUpdateLoanDto)
                .collect(Collectors.toList());
    }

    public long countActiveLoansByUserId(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return loanRepository.countByUserAndStatus(user, LoanEntity.LoanStatus.ACTIVE);
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
}