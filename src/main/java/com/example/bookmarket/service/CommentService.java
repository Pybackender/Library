package com.example.bookmarket.service;

import com.example.bookmarket.entity.CommentEntity;
import com.example.bookmarket.entity.UserEntity;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.exception.CommentNotFoundException;
import com.example.bookmarket.exception.UserNotFoundException;
import com.example.bookmarket.repository.CommentRepository;
import com.example.bookmarket.repository.UserRepository;
import com.example.bookmarket.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public CommentEntity addComment(Long userId, Long bookId, String content, int rating) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        CommentEntity comment = new CommentEntity();
        comment.setUser(user);
        comment.setBook(book);
        comment.setContent(content);
        comment.setRating(rating);

        return commentRepository.save(comment);
    }

    public List<CommentEntity> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException(commentId); // ایجاد استثنا در صورت عدم وجود نظر
        }
        commentRepository.deleteById(commentId);
    }

}