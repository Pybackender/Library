//package com.example.bookmarket.service;
//
//import com.example.bookmarket.entity.UserEntity;
//import com.example.bookmarket.repository.UserRepository;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserEntity userEntity = userRepository.findByUsername(username);
//        if (userEntity == null) {
//            throw new UsernameNotFoundException("User not found with username: " + username);
//        }
//
//        return User.builder()
//                .username(userEntity.getUsername())
//                .password(userEntity.getPassword())
//                .authorities("USER")  // اینجا authority می‌دهیم
//                .build();
//    }
//}