package com.example.bookmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "librarian")
public class LibrarianEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true) // اضافه کردن unique
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password is mandatory")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibrarianStatus status = LibrarianStatus.INACTIVE; // تغییر به INACTIVE

    public enum LibrarianStatus {
        ACTIVE,
        INACTIVE
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "librarian_roles", joinColumns = @JoinColumn(name = "librarian_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // Constructor برای تنظیم خودکار نقش ADMIN
    public LibrarianEntity() {
        this.roles.add("ADMIN"); // به طور خودکار نقش ADMIN اضافه شود
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    // اضافه کردن متد کمکی برای افزودن نقش
    public void addRole(String role) {
        this.roles.add(role);
    }
}