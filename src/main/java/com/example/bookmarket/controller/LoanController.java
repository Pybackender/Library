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

    @Operation(summary = "امانت گرفتن کتاب")
    @PostMapping("/add")
    public ResponseEntity<LoanDto> createLoan(@RequestBody LoanDto loanDto) {
        LoanDto loan = loanService.createLoan(loanDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @Operation(summary = "پیدا کردن امانتی")
    @GetMapping("/{loanId}")
    public ResponseEntity<UpdateLoanDto> getLoan(@Valid @PathVariable Long loanId) {
        UpdateLoanDto loan = loanService.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "پیدا کردن امانتی ها")
    @GetMapping("/all")
    public ResponseEntity<List<UpdateLoanDto>> getAllLoans() {
        List<UpdateLoanDto> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "بروزرسانی کردن امانت")
    @PutMapping("/update")
    public ResponseEntity<UpdateLoanDto> updateLoan(@RequestBody UpdateLoanDto updateLoanDto) {
        UpdateLoanDto updatedLoan = loanService.updateLoan(updateLoanDto);
        return ResponseEntity.ok(updatedLoan);
    }

    @Operation(summary = "پاک کردن امانت")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "برگشت دادن امانتی")
    @DeleteMapping("/return/{loanId}")
    public ResponseEntity<Void> returnBook(@PathVariable Long loanId) {
        loanService.returnLoan(loanId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "وضعیت امانت ها")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getLoanStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("activeLoans", loanService.countActiveLoans());
        stats.put("returnedLoans", loanService.countReturnedLoans());
        stats.put("totalLoans", loanService.countTotalLoans());

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "پیدا کردن امانت با فیلتر")
    @GetMapping("/search")
    public ResponseEntity<List<UpdateLoanDto>> searchLoans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanEntity.LoanStatus status) {

        List<UpdateLoanDto> loans;

        if (userId != null) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            loans = loanService.getLoansByUserId(userId);
        } else if (bookId != null) {
            BookEntity book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new BookNotFoundException(bookId));
            loans = loanService.getLoansByBookId(bookId);
        } else if (status != null) {
            loans = loanService.getLoansByStatus(status);
        } else {
            loans = loanService.getAllLoans();
        }

        return ResponseEntity.ok(loans);
    }
}