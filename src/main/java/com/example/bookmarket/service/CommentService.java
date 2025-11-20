package com.example.bookmarket.service;

import com.example.bookmarket.dto.CommentDto;
import com.example.bookmarket.entity.CommentEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.exception.CommentNotFoundException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.CommentRepository;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public CommentDto addComment(CommentDto commentDto) {
        UserEntity user = userRepository.findById(commentDto.userId())
                .orElseThrow(() -> new UserNotFoundException(commentDto.userId()));

        BookEntity book = bookRepository.findById(commentDto.bookId())
                .orElseThrow(() -> new BookNotFoundException(commentDto.bookId()));

        CommentEntity comment = new CommentEntity();
        comment.setUser(user);
        comment.setBook(book);
        comment.setContent(commentDto.content());
        comment.setRating(commentDto.rating());

        CommentEntity savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    public List<CommentDto> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookId(bookId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException(commentId);
        }
        commentRepository.deleteById(commentId);
    }

    // متد تبدیل Entity به DTO
    private CommentDto convertToDto(CommentEntity comment) {
        return new CommentDto(
                comment.getUser().getId(),
                comment.getBook().getId(),
                comment.getContent(),
                comment.getRating()
        );
    }
}