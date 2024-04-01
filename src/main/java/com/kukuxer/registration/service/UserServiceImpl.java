package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.user.Role;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.repository.UserStatisticRepository;
import com.kukuxer.registration.service.interfaces.UserService;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatisticRepository userStatisticRepository;

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
            userRepository.save(user);
            createStatistic(user);
            return user;
        } catch (IllegalStateException e) {
            logger.error("Failed to create user: {}", e.getMessage());
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create user", e);
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
            logger.error("Failed to retrieve user by ID {}: {}", id, e.getMessage());
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by ID {}", id, e);
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
            logger.error("Failed to retrieve user by username {}: {}", username, e.getMessage());
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by username {}", username, e);
            Sentry.captureException(e);
            throw new RuntimeException("Failed to retrieve user. Please try again later.");
        }
    }
    @Override
    public void updateUsersInGame(User user1, User user2){
        if(user1.isInGame()!=user2.isInGame()){
            throw new RuntimeException("готовил пшеницу.");
        }
        user1.setInGame(!user1.isInGame());
        user2.setInGame(!user2.isInGame());
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Override
    @Transactional
    public void createStatistic(User user) {
        UserStatistic userStatistic = UserStatistic.builder()
                .user(user)
                .totalGamesPlayed(0)
                .wins(0)
                .losses(0)
                .draws(0)
                .rating(1000)
                .confidence(115)
                .build();

        try {
            userStatisticRepository.save(userStatistic);
        } catch (DataIntegrityViolationException e) {
            logger.error("Failed to create user statistic: {}", e.getMessage());
            Sentry.captureException(e);
            throw new RuntimeException("Failed to create user statistic");
        } catch (Exception e) {
            logger.error("Failed to create user statistic", e);
            Sentry.captureException(e);
            throw new RuntimeException("Failed to create user statistic due to persistence error.");
        }
    }
}
