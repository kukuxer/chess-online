package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.user.Role;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.FriendRequestRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatisticRepository userStatisticRepository;
    private final FriendRequestRepository friendRequestRepository;

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
    public User getByEmail(String email) {
        try {
            return userRepository.findByEmail(email).orElseThrow(
                    () -> new EntityNotFoundException("User not found with email: " + email)
            );
        } catch (EntityNotFoundException e) {
            logger.error("Failed to retrieve user by email {}: {}", email, e.getMessage());
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by email {}", email, e);
            Sentry.captureException(e);
            throw new RuntimeException("Failed to retrieve user. Please try again later.");
        }
    }

    @Override
    public void updateUsersInGame(User user1, User user2) {
        if (user1.isInGame() != user2.isInGame()) {
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

    @Override
    public FriendRequest sendFriendRequest(Long userId, Long senderId) {
        if (userId.equals(senderId)) throw new RuntimeException("You can not send a friend request to yourself.");
        boolean isPresent = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(senderId, userId)
                .isPresent();
        if (isPresent){
            throw new RuntimeException("You cannot send request twice to one user");
        }
        List<FriendRequest> friendRequests = getAllBySenderId(userId);
        for (FriendRequest fR : friendRequests){
            if(fR.getReceiverId().equals(senderId)){
                acceptFriendRequest(fR.getFriendRequestId());
            }
           return fR ;
        }
        FriendRequest friendRequest = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(userId)
                .status("PENDING")
                .build();
        friendRequestRepository.save(friendRequest);
        return friendRequest;
    }

    @Override
    public void acceptFriendRequest(Long friendRequestId) {
        FriendRequest friendRequest = findFriendRequestById(friendRequestId);
        if (friendRequest.getStatus().equals("PENDING")) {
            friendRequest.setStatus("ACCEPTED");
            Long senderId = friendRequest.getSenderId();
            Long receiverId = friendRequest.getReceiverId();
            User sender = userRepository.findById(senderId).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElseThrow();
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            userRepository.save(sender);
            userRepository.save(receiver);
            friendRequestRepository.save(friendRequest);
        } else {
            throw new RuntimeException("You can not accept non pending request.");
        }
    }

    @Override
    public void rejectFriendRequest(Long friendRequestId) {
        FriendRequest friendRequest = findFriendRequestById(friendRequestId);
        if (friendRequest.getStatus().equals("PENDING")) {
            friendRequest.setStatus("REJECTED");
        } else {
            throw new RuntimeException("You can not reject non pending request.");
        }
        friendRequestRepository.save(friendRequest);
    }

    @Override
    public FriendRequest findFriendRequestById(Long friendRequestId) {
        return friendRequestRepository.findById(friendRequestId).orElseThrow(
                () -> new RuntimeException("Could not find friend request with id " + friendRequestId)
        );
    }

    @Override
    public List<FriendRequest> getAllByReceiverId(Long receiverId) {
        return friendRequestRepository.findAllByReceiverId(receiverId);
    }

    @Override
    public List<FriendRequest> getAllBySenderId(Long senderId) {
        return friendRequestRepository.findAllBySenderId(senderId);
    }

}
