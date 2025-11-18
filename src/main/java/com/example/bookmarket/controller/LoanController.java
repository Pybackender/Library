package com.example.bookmarket.controller;

import com.example.bookmarket.dto.LoanDto;
import com.example.bookmarket.dto.UpdateLoanDto;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.entity.LoanEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.exception.*;
import com.example.bookmarket.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.BookRepository;
import com.example.bookmarket.repository.LoanRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "مدیریت امانت ها", description = "")
public class LoanController {
    @Autowired
    private LoanService loanService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;
    @Operation(summary = "امانت گرفتن کتاب  ")
    @PostMapping("/add")
    public ResponseEntity<LoanEntity> createLoan(@RequestBody LoanDto loanDto) {
        LoanEntity loan = loanService.createLoan(loanDto.userId(), loanDto.bookId(), loanDto.dueDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }
    @Operation(summary = "پیدا کردن امانتی ")
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanEntity> getLoan(@Valid @PathVariable Long loanId) {
        return loanService.findById(loanId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new LoanNotFoundException(loanId)); // Throw the custom exception
    }
    @Operation(summary = "پیدا کردن امانتی ها ")
    @GetMapping("/all")
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        List<LoanEntity> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }
    @Operation(summary = "بروزرسانی کردن امانت ")
    @PutMapping("/update")
    public ResponseEntity<LoanEntity> updateLoan(@RequestBody UpdateLoanDto updateLoanDto) {
        LoanEntity updatedLoan = loanService.updateLoan(updateLoanDto.id(), updateLoanDto.userId(), updateLoanDto.bookId(), updateLoanDto.dueDate());
        return ResponseEntity.ok(updatedLoan);
    }

//    @DeleteMapping("/delete")
//    public ResponseEntity<Void> deleteLoan(@RequestBody Long id) {
//        loanService.deleteLoan(id);
//        return ResponseEntity.noContent().build();
//    }
    // next way for delete
    @Operation(summary = "پاک کردن امانت ")
    @DeleteMapping("/delete/{id}") // just change address
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "برگشت دادن امانتی ")
    @DeleteMapping("/return/{loanId}")
    public ResponseEntity<Void> returnBook(@PathVariable Long loanId) {
        loanService.returnLoan(loanId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    @Operation(summary = "وضعیت امانت ها ")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getLoanStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("activeLoans", loanService.countActiveLoans());
        stats.put("returnedLoans", loanService.countReturnedLoans());
        stats.put("totalLoans", loanService.countTotalLoans());

        return ResponseEntity.ok(stats);
    }
    @Operation(summary = "پیدا کردن امانت با فیلتر ")
    @GetMapping("/search")
    public ResponseEntity<List<LoanEntity>> searchLoans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanEntity.LoanStatus status) {

        List<LoanEntity> loans;

        if (userId != null) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            loans = loanService.getLoansByUserId(userId);
        } else if (bookId != null) {
            BookEntity book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new BookNotFoundException(bookId));
            loans = loanService.getLoansByBookId(bookId);
        } else if (status != null) {
            loans = loanRepository.findByStatus(status);
        } else {
            loans = loanService.getAllLoans(); // در صورت عدم ارائه هیچ پارامتر
        }

        return ResponseEntity.ok(loans);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<String> handleLoanNotFound(LoanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<String> handleLoanNotFound(LimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @ExceptionHandler(BookAlreadyReturnedException.class)
    public ResponseEntity<String> handleLoanNotFound(BookAlreadyReturnedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<String> handleUserInactive(UserInactiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    // Exception handler for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + ", " + second)
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(errorMessage);
    }
}


// خروجی کنترولر باید  dto باشه نباید entity  باشه