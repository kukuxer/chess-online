package com.kukuxer.registration.controller;


import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.FriendRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.UserService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final MatchService matchService;
    private final UserService userService;

    @GetMapping("/whatIsMyUsername")
    public ResponseEntity<String> showUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // If it is, cast it to UserDetails and retrieve the username
            String principalUsername = ((UserDetails) principal).getUsername();
            String responseMessage = "Your principalUsername is: " + principalUsername + ", Username: " + username;
            return ResponseEntity.ok(responseMessage);
        } else {
            // If it's not a UserDetails object, you might handle it differently based on your application logic
            Sentry.captureMessage("Failed to get username");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to retrieve username");
        }
    }

    @GetMapping("/matches")
    public List<Match> getAllMatchesByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        return matchService.getAllMatchesByUser(user);
    }

    @PostMapping("/sendFriendRequest/{userId}")
    public FriendRequest sendFriendRequest(@PathVariable("userId")Long userId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long senderId = userService.getByUsername(username).getId();
        return userService.sendFriendRequest(userId, senderId);
    }
}
