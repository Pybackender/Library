package com.example.bookmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "librarian")
public class LibrarianEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password is mandatory")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibrarianStatus status = LibrarianStatus.ACTIVE; // وضعیت اولیه

    public enum LibrarianStatus {
        ACTIVE,
        INACTIVE
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public LibrarianStatus getStatus() {
        return status;
    }

    public void setStatus(LibrarianStatus status) {
        this.status = status;
    }
}