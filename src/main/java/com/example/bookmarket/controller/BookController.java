package com.example.bookmarket.controller;

import com.example.bookmarket.dto.AddBookDto;
import com.example.bookmarket.dto.UpdateBookDto;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.exception.BookOutOfStockException;
import com.example.bookmarket.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/books")
@Tag(name = "مدیریت کتاب ها", description = "")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    @Operation(summary = "اضافه کردن کتاب ")
    @PostMapping("/add")
    public ResponseEntity<Boolean> add(@Valid @RequestBody AddBookDto addBookDto) {
        boolean createdUser = bookService.add(addBookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @Operation(summary = "پیدا کردن کتاب با ID ")
    @GetMapping("/{bookId}")
    public ResponseEntity<BookEntity> getBook(@Valid @PathVariable Long bookId) {
        return bookService.findById(bookId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BookNotFoundException(bookId)); // Throw the custom exception
    }
    @Operation(summary = "بروزرسانی کردن کتاب ")
    @PutMapping("/update")
    public ResponseEntity<Boolean> update(@Valid @RequestBody UpdateBookDto updateBookDto) {
        boolean updatedBook = bookService.update(updateBookDto);
        return ResponseEntity.ok(updatedBook);
    }
    @Operation(summary = "پاک کردن کتاب ")
    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<Boolean> delete(@Valid @PathVariable Long bookId) {
        boolean deleted = bookService.delete(bookId);
        return ResponseEntity.ok(deleted);
    }
    @Operation(summary = "پیدا کردن کتاب ها ")
    @GetMapping("/all")
    public ResponseEntity<List<BookEntity>> getAllBooks() {
        List<BookEntity> books = bookService.findAll();
        return ResponseEntity.ok(books); // Return the list of books with a 200 OK status
    }
    @Operation(summary = "پیدا کردن کتاب ها با فیلتر ")
    @GetMapping("/search")
    public ResponseEntity<List<BookEntity>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<BookEntity> books;

        // جستجوی بر اساس عنوان
        if (title != null) {
            books = bookService.searchByTitle(title);
        } else if (author != null) {
            books = bookService.searchByAuthor(author);
        } else if (genre != null) {
            books = bookService.searchByGenre(genre);
        } else {
            books = bookService.findAll(); // اگر هیچ پارامتر جستجویی وجود نداشت، همه کتاب‌ها را برگردانید
        }

        // فیلتر بر اساس قیمت
        if (minPrice != null || maxPrice != null) {
            books = books.stream()
                    .filter(book -> (minPrice == null || book.getFinalPrice().compareTo(minPrice) >= 0) &&
                            (maxPrice == null || book.getFinalPrice().compareTo(maxPrice) <= 0))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(books);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + ", " + second)
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(errorMessage);
    }
    @RestControllerAdvice
    public class GlobalExceptionHandler {
        @ExceptionHandler(BookOutOfStockException.class)
        public ResponseEntity handleBookOutOfStock(BookOutOfStockException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

}
