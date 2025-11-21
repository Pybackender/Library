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
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public AddBookDto add(AddBookDto addBookDto) {
        if (bookRepository.existsByTitle(addBookDto.title())) {
            throw new RuntimeException("Book with title '" + addBookDto.title() + "' already exists");
        }

        var bookEntity = new BookEntity();
        bookEntity.setTitle(addBookDto.title());
        bookEntity.setAuthor(addBookDto.author());
        bookEntity.setPrice(addBookDto.price());
        bookEntity.setNumberOfBooks(addBookDto.numberOfBooks());
        bookEntity.setPublishDate(addBookDto.publishedDate());
        bookEntity.setGenre(addBookDto.genre());
        bookEntity.setVolume(addBookDto.volume());
        bookEntity.setDiscountPercentage(addBookDto.discountPercentage());

        BookEntity savedBook = bookRepository.save(bookEntity);
        return convertToAddBookDto(savedBook);
    }

    @Transactional(readOnly = true)
    public Optional<UpdateBookDto> findById(Long bookId) {
        return bookRepository.findById(bookId)
                .map(this::convertToUpdateBookDto);
    }

    @Transactional
    public UpdateBookDto update(UpdateBookDto updateBookDto) {
        BookEntity bookEntity = bookRepository.findById(updateBookDto.id())
                .orElseThrow(() -> new BookNotFoundException(updateBookDto.id()));

        bookEntity.setTitle(updateBookDto.title());
        bookEntity.setAuthor(updateBookDto.author());
        bookEntity.setPrice(updateBookDto.price());
        bookEntity.setNumberOfBooks(updateBookDto.numberOfBooks());
        bookEntity.setPublishDate(updateBookDto.publishedDate());
        bookEntity.setGenre(updateBookDto.genre());
        bookEntity.setVolume(updateBookDto.volume());
        bookEntity.setDiscountPercentage(updateBookDto.discountPercentage());

        BookEntity updatedBook = bookRepository.save(bookEntity);
        return convertToUpdateBookDto(updatedBook);
    }

    @Transactional
    public void delete(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }
        bookRepository.deleteById(bookId);
    }

    @Transactional(readOnly = true)
    public List<UpdateBookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(this::convertToUpdateBookDto)
                .collect(Collectors.toList());
    }

    public List<UpdateBookDto> searchBooks(String title, String author, String genre,
                                           BigDecimal minPrice, BigDecimal maxPrice) {
        List<BookEntity> books;

        if (title != null) {
            books = bookRepository.findByTitleContainingIgnoreCase(title);
        } else if (author != null) {
            books = bookRepository.findByAuthorContainingIgnoreCase(author);
        } else if (genre != null) {
            books = bookRepository.findByGenre(genre);
        } else {
            books = bookRepository.findAll();
        }

        if (minPrice != null || maxPrice != null) {
            books = books.stream()
                    .filter(book -> (minPrice == null || book.getFinalPrice().compareTo(minPrice) >= 0) &&
                            (maxPrice == null || book.getFinalPrice().compareTo(maxPrice) <= 0))
                    .collect(Collectors.toList());
        }

        return books.stream()
                .map(this::convertToUpdateBookDto)
                .collect(Collectors.toList());
    }

    private AddBookDto convertToAddBookDto(BookEntity book) {
        return new AddBookDto(
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getNumberOfBooks(),
                book.getPublishDate(),
                book.getGenre(),
                book.getVolume(),
                book.getDiscountPercentage()
        );
    }

    private UpdateBookDto convertToUpdateBookDto(BookEntity book) {
        return new UpdateBookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getNumberOfBooks(),
                book.getPublishDate(),
                book.getGenre(),
                book.getVolume(),
                book.getDiscountPercentage()
        );
    }

    public List<UpdateBookDto> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToUpdateBookDto)
                .collect(Collectors.toList());
    }

    public List<UpdateBookDto> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author).stream()
                .map(this::convertToUpdateBookDto)
                .collect(Collectors.toList());
    }

    public List<UpdateBookDto> searchByGenre(String genre) {
        return bookRepository.findByGenre(genre).stream()
                .map(this::convertToUpdateBookDto)
                .collect(Collectors.toList());
    }
}