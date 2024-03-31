package com.kukuxer.registration.service;


import com.kukuxer.registration.domain.user.Role;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.UserService;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(User user) {
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new IllegalStateException("User with username " + user.getUsername() + " already exists.");
            }
            user.setRoles(Collections.singleton(Role.USER));
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        } catch (IllegalStateException e) {
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Failed to create user. Please try again later.");
        }
    }

    @Override
    public User getById(Long id) {
        try {
            return userRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("User not found with ID: " + id)
            );
        } catch (EntityNotFoundException e) {
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Failed to retrieve user. Please try again later.");
        }
    }

    @Override
    public User getByUsername(String username) {
        try {
            return userRepository.findByUsername(username).orElseThrow(
                    () -> new EntityNotFoundException("User not found with username: " + username)
            );
        } catch (EntityNotFoundException e) {
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Failed to retrieve user. Please try again later.");
        }
    }
}
