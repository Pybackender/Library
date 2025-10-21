package com.example.bookmarket.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.time.ZonedDateTime;
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

    @Column(name = "numberofbooks") // Ensure the column name matches
    private Integer numberOfBooks;

    @Column(nullable = false) // فیلد برای جلد
    private Integer volume;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publish_date")
    private Date publishDate;

    @Column(nullable = false) // فیلد جدید برای ژانر
    private String genre;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "register_date", updatable = false)
    private ZonedDateTime registerDate;

    @Column(name = "discount_percentage") // فیلد جدید برای درصد تخفیف
    private Integer discountPercentage; // به درصد تخفیف به صورت عدد صحیح

    @Column(name = "final_price") // فیلد جدید برای قیمت نهایی
    private BigDecimal finalPrice;

    @PrePersist
    protected void onCreate() {
        registerDate = ZonedDateTime.now(ZoneId.of("Asia/Tehran")); // زمان کنونی برحسب زمان ایران
        updateFinalPrice(); // قیمت نهایی را به‌روز کنید
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

    private void updateFinalPrice() {
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

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getNumberOfBooks() {
        return numberOfBooks;
    }

    public void setNumberOfBooks(Integer numberOfBooks) {
        this.numberOfBooks = numberOfBooks;
    }

    public ZonedDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(ZonedDateTime registerDate) {
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