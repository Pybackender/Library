package com.example.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Data
@NoArgsConstructor
@Table(name = "book")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "numberofbooks")
    private Integer numberOfBooks;

    @Column(nullable = false)
    private Integer volume;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(nullable = false)
    private String genre;

    @Column(name = "register_date", updatable = false)
    private LocalDateTime registerDate;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @PrePersist
    protected void onCreate() {
        registerDate = LocalDateTime.now(ZoneId.of("Asia/Tehran"));
    }

}