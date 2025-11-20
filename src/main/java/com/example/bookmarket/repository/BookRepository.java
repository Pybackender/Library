package com.example.bookmarket.repository;

import com.example.bookmarket.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    boolean existsByTitle(String title);
    List<BookEntity> findByTitleContainingIgnoreCase(String title);
    List<BookEntity> findByAuthorContainingIgnoreCase(String author);
    List<BookEntity> findByGenre(String genre);

}