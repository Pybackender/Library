package com.example.bookmarket.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "loan")
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false,name = "final_price")
    private BigDecimal finalPrice;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "loan_date", nullable = false)
    private Date loanDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date", nullable = false)
    private Date dueDate;

    public enum LoanStatus {
        ACTIVE,
        RETURNED
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE; // تنظیم وضعیت اولیه

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        loanDate = new Date();
    }

    // Default constructor
    public LoanEntity() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public Date getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(Date loanDate) {
        this.loanDate = loanDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
}
