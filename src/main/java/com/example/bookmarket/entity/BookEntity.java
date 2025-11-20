package com.example.bookmarket.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
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

    @Column(name = "discount_percentage") // فیلد جدید برای درصد تخفیف
    private Integer discountPercentage; // به درصد تخفیف به صورت عدد صحیح

    @Column(name = "final_price") // فیلد جدید برای قیمت نهایی
    private BigDecimal finalPrice;

    @PrePersist
    protected void onCreate() {
        registerDate = LocalDateTime.now(ZoneId.of("Asia/Tehran"));
        updateFinalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        updateFinalPrice(); // قیمت نهایی را به‌روز کنید
    }


    // متد برای محاسبه قیمت نهایی پس از اعمال تخفیف
    public BigDecimal getDiscountedPrice() {
        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal discountAmount = price.multiply(BigDecimal.valueOf(discountPercentage)).divide(BigDecimal.valueOf(100));
            return price.subtract(discountAmount);
        }
        return price; // اگر تخفیف وجود ندارد، قیمت اصلی را برگردانید
    }

    void updateFinalPrice() {
        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal discountAmount = price.multiply(BigDecimal.valueOf(discountPercentage)).divide(BigDecimal.valueOf(100));
            finalPrice = price.subtract(discountAmount);
        } else {
            finalPrice = price; // اگر تخفیف وجود ندارد، قیمت اصلی را برگردانید
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getNumberOfBooks() {
        return numberOfBooks;
    }

    public void setNumberOfBooks(Integer numberOfBooks) {
        this.numberOfBooks = numberOfBooks;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
}