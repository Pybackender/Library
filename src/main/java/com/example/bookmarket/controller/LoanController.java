package com.example.bookmarket.controller;

import com.example.bookmarket.dto.LoanDto;
import com.example.bookmarket.dto.UpdateLoanDto;
import com.example.bookmarket.enums.LoanStatus;
import com.example.bookmarket.exception.LoanNotFoundException;
import com.example.bookmarket.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "مدیریت امانت ها", description = "")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "امانت گرفتن کتاب")
    @PostMapping("/add")
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody LoanDto loanDto) {
        LoanDto loan = loanService.createLoan(loanDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @Operation(summary = "پیدا کردن امانتی")
    @GetMapping("/{loanId}")
    public ResponseEntity<UpdateLoanDto> getLoan(@PathVariable Long loanId) {
        UpdateLoanDto loan = loanService.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "مشاهده همه ی امانتی ها")
    @GetMapping("/all")
    public ResponseEntity<List<UpdateLoanDto>> getAllLoans() {
        List<UpdateLoanDto> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "بروزرسانی کردن امانت")
    @PutMapping("/update")
    public ResponseEntity<UpdateLoanDto> updateLoan(@Valid @RequestBody UpdateLoanDto updateLoanDto) {
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
            @RequestParam(required = false) LoanStatus status) {

        List<UpdateLoanDto> loans = loanService.searchLoans(userId, bookId, status);
        return ResponseEntity.ok(loans);
    }
}