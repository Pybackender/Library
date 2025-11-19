package com.example.bookmarket.controller;

import com.example.bookmarket.dto.AddBookDto;
import com.example.bookmarket.dto.UpdateBookDto;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/v1/books")
@Tag(name = "مدیریت کتاب ها", description = "")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "اضافه کردن کتاب")
    @PostMapping("/add")
    public ResponseEntity<AddBookDto> add(@Valid @RequestBody AddBookDto addBookDto) {
        AddBookDto createdBook = bookService.add(addBookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @Operation(summary = "پیدا کردن کتاب با ID")
    @GetMapping("/{bookId}")
    public ResponseEntity<UpdateBookDto> getBook(@PathVariable Long bookId) {
        UpdateBookDto book = bookService.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "بروزرسانی کردن کتاب")
    @PutMapping("/update")
    public ResponseEntity<UpdateBookDto> update(@Valid @RequestBody UpdateBookDto updateBookDto) {
        UpdateBookDto updatedBook = bookService.update(updateBookDto);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "پاک کردن کتاب")
    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable Long bookId) {
        bookService.delete(bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "پیدا کردن همه کتاب ها")
    @GetMapping("/all")
    public ResponseEntity<List<UpdateBookDto>> getAllBooks() {
        List<UpdateBookDto> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "پیدا کردن کتاب ها با فیلتر")
    @GetMapping("/search")
    public ResponseEntity<List<UpdateBookDto>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<UpdateBookDto> books = bookService.searchBooks(title, author, genre, minPrice, maxPrice);
        return ResponseEntity.ok(books);
    }
}