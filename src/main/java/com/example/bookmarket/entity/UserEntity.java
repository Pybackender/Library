package com.example.bookmarket.entity;

import com.example.bookmarket.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
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
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    public UserEntity() {
        this.roles.add("USER");
    }

    @Column(name = "register_date", updatable = false)
    private LocalDateTime registerDate;

    @PostLoad
    private void initRoles() {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        if (this.roles.isEmpty()) {
            this.roles.add("USER");
        }
    }

    @PrePersist
    protected void onCreate() {
        registerDate = LocalDateTime.now();
    }

    // Getters و Setters
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

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}