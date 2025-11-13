package com.example.bookmarket.controller;

import com.example.bookmarket.dto.CommentDto;
import com.example.bookmarket.entity.CommentEntity;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.exception.CommentNotFoundException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "مدیریت کامنت ها", description = "")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @Operation(summary = "اضافه کردن کامنت ")
    @PostMapping("/add")
    public ResponseEntity<CommentEntity> addComment(@Valid @RequestBody CommentDto commentDto) {
        CommentEntity comment = commentService.addComment(
                commentDto.userId(), commentDto.bookId(), commentDto.content(), commentDto.rating()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    @Operation(summary = "پیدا کردن کامنت ")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<CommentEntity>> getCommentsByBookId(@PathVariable Long bookId) {
        List<CommentEntity> comments = commentService.getCommentsByBookId(bookId);
        return ResponseEntity.ok(comments);
    }
    @Operation(summary = "پاک کردن کامنت ")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFound(CommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    if ("rating".equals(error.getField())) {
                        return "rating : امتیاز باید بین ۱ تا ۵ باشد.";
                    }
                    return error.getField() + ": " + error.getDefaultMessage();
                })
                .reduce((first, second) -> first + ", " + second)
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(errorMessage);
    }
}