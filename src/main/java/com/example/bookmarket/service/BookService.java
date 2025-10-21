package com.example.bookmarket.service;

import com.example.bookmarket.dto.AddBookDto;
import com.example.bookmarket.dto.UpdateBookDto;
import com.example.bookmarket.entity.BookEntity;
import com.example.bookmarket.exception.BookNotFoundException;
import com.example.bookmarket.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public boolean add(AddBookDto addBookDto) {
        if (bookRepository.existsByTitle(addBookDto.title())) {
            return false; // Book already exists
        }

        var bookEntity = new BookEntity();
        bookEntity.setTitle(addBookDto.title());
        bookEntity.setAuthor(addBookDto.author());
        bookEntity.setPrice(addBookDto.price());
        bookEntity.setNumberOfBooks(addBookDto.numberOfBooks());
        bookEntity.setGenre(addBookDto.genre());
        bookEntity.setVolume(addBookDto.volume());
        bookEntity.setDiscountPercentage(addBookDto.discountPercentage());

        bookRepository.save(bookEntity); // Save the book
        return true; // Return true on successful save
    }
    @Transactional
    public Optional<BookEntity> findById(Long bookId) {
        return bookRepository.findById(bookId); // Return an Optional containing the book if found
    }

    @Transactional
    public boolean update(UpdateBookDto updateBookDto) {
        // Fetch the book by ID
        BookEntity bookEntity = bookRepository.findById(updateBookDto.id())
                .orElseThrow(() -> new BookNotFoundException(updateBookDto.id()));

        // Update the fields of the book entity
        bookEntity.setTitle(updateBookDto.title());
        bookEntity.setAuthor(updateBookDto.author());
        bookEntity.setPrice(updateBookDto.price());  // Use accessor method
        bookEntity.setNumberOfBooks(updateBookDto.numberOfBooks()); // Use accessor method
        bookEntity.setGenre(updateBookDto.genre());
        bookEntity.setVolume(updateBookDto.volume());
        bookEntity.setDiscountPercentage(updateBookDto.discountPercentage());

        // Save the updated book entity
        bookRepository.save(bookEntity);
        return true;
    }

    @Transactional
    public boolean delete(Long bookId) {
        if (bookRepository.existsById(bookId)) {
            bookRepository.deleteById(bookId);
            return true;
        } else {
            throw new BookNotFoundException(bookId);
        }
    }

    @Transactional(readOnly = true)
    public List<BookEntity> findAll() {
        return bookRepository.findAll(); // Fetch all books from the repository
    }

    public List<BookEntity> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<BookEntity> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<BookEntity> searchByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

}
