package com.example.bookmarket.controller;

import com.example.bookmarket.dto.CommentDto;
import com.example.bookmarket.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "اضافه کردن کامنت")
    @PostMapping("/add")
    public ResponseEntity<CommentDto> addComment(@Valid @RequestBody CommentDto commentDto) {
        CommentDto comment = commentService.addComment(commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Operation(summary = "پیدا کردن کامنت")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<CommentDto>> getCommentsByBookId(@PathVariable Long bookId) {
        List<CommentDto> comments = commentService.getCommentsByBookId(bookId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "پاک کردن کامنت")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}