package com.kukuxer.registration.service;


import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new IllegalStateException("User already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }
    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(
                RuntimeException::new
        );
    }
    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                ()->new RuntimeException("User not found.")
        );
    }
}
