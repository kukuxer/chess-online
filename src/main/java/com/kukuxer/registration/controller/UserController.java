package com.kukuxer.registration.controller;


import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.requests.Request;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.UserStatisticRepository;
import com.kukuxer.registration.security.JwtResponse;
import com.kukuxer.registration.service.UserServiceImpl;
import com.kukuxer.registration.service.interfaces.AuthService;
import com.kukuxer.registration.service.interfaces.MatchService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final MatchService matchService;
    private final UserServiceImpl userService;
    private final AuthService authService;
    private final UserStatisticRepository userStatisticRepository;


    @GetMapping("/showMyProfile")
    public ResponseEntity<?> showProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ResponseEntity.ok(user);
        } else {
            // If it's not a UserDetails object, you might handle it differently based on your application logic
            Sentry.captureMessage("Failed to get username");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to retrieve username");
        }
    }

    @GetMapping("/showMyStatistic")
    public ResponseEntity<?> showMyStatistic() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            Sentry.captureMessage("Failed to get username or not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to retrieve username or not authenticated");
        }

        User user = userService.getByUsername(authentication.getName());
        Optional<UserStatistic> userStatistic = userStatisticRepository.findByUser(user);

        if (userStatistic.isPresent()) {
            return ResponseEntity.ok(userStatistic.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User statistic not found");
        }
    }

    @GetMapping("/showMyFriends")
    public ResponseEntity<?> showMyFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            Sentry.captureMessage("Failed to get username or not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to retrieve username or not authenticated");
        }

        User user = userService.getByUsername(authentication.getName());
        return ResponseEntity.ok(user.getFriends());
    }

    @GetMapping("/showFriendRequestsSendedToMe")
    public ResponseEntity<List<FriendRequest>> getReceivedFriendRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long userId = userService.getByUsername(currentUsername).getId();

        List<FriendRequest> friendRequests = userService.getAllFriendRequestByReceiverId(userId);

        return ResponseEntity.ok(friendRequests);
    }

    @GetMapping("/showFriendRequestsThatISent")
    public ResponseEntity<List<FriendRequest>> getSentFriendRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long userId = userService.getByUsername(currentUsername).getId();

        List<FriendRequest> friendRequests = userService.getAllFriendRequestBySenderId(userId);

        return ResponseEntity.ok(friendRequests);
    }
    @GetMapping("/showMatchRequestsSendedToMe")
    public ResponseEntity<List<Request>> getReceivedRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long userId = userService.getByUsername(currentUsername).getId();

        List<Request> friendRequests = userService.getAllRequestByReceiverId(userId);

        return ResponseEntity.ok(friendRequests);
    }

    @GetMapping("/showMatchRequestsThatISent")
    public ResponseEntity<List<Request>> getSentRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long userId = userService.getByUsername(currentUsername).getId();

        List<Request> friendRequests = userService.getAllRequestBySenderId(userId);

        return ResponseEntity.ok(friendRequests);
    }

    @PostMapping("/refresh")
    public JwtResponse refresh() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        return authService.refresh(user);
    }

    @GetMapping("/matches")
    public List<Match> getAllMatchesByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        return matchService.getAllMatchesByUser(user);
    }

    @PostMapping("/sendFriendRequest/{userId}")
    public FriendRequest sendFriendRequest(@PathVariable("userId") Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long senderId = userService.getByUsername(username).getId();
        return userService.sendFriendRequest(userId, senderId);
    }

    @PostMapping("/acceptFriendRequest/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable("requestId") Long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.getByUsername(authentication.getName()).getId();

        FriendRequest friendRequest = userService.findFriendRequestById(requestId);

        if (!friendRequest.getReceiverId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only accept friend requests sent to you.");
        }

        userService.acceptFriendRequest(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully accepted the friend request.");
    }

    @PostMapping("/rejectFriendRequest/{requestId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable("requestId") Long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long userId = userService.getByUsername(currentUsername).getId();
        FriendRequest friendRequest = userService.findFriendRequestById(requestId);
        if (friendRequest.getSenderId().equals(userId)) {
            throw new RuntimeException("You can not reject your own friend request.");
        }
        userService.rejectFriendRequest(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully rejected the friend request.");
    }
}
