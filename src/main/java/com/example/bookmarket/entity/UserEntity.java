package com.example.bookmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "User_")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "UserName is mandatory")
    private String username;

    @Column(nullable = false)
    @NotNull(message = "PassWord is mandatory")
    private String password;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserStatus status; // اضافه کردن این خط

    public enum UserStatus {
        ACTIVE,
        INACTIVE
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(); // فیلد نقش‌ها

    public UserEntity() {
        this.roles.add("USER"); // به طور خودکار نقش USER اضافه شود
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "register_date", updatable = false)
    private Date registerDate;

    @PrePersist
    protected void onCreate() {
        registerDate = new Date();
    }

    // Getters و Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "UserName is mandatory") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "UserName is mandatory") String username) {
        this.username = username;
    }

    public @NotNull(message = "PassWord is mandatory") String getPassword() {
        return password;
    }

    public void setPassword(@NotNull(message = "PassWord is mandatory") String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}