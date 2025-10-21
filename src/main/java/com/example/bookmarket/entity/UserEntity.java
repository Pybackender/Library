package com.example.bookmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "register_date", updatable = false)
    private Date registerDate;

    @PrePersist
    protected void onCreate() {
        registerDate = new Date();
    }

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

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

}
