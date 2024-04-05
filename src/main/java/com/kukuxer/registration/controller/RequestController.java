package com.kukuxer.registration.controller;


import com.kukuxer.registration.domain.request.SearchRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import com.kukuxer.registration.service.interfaces.SearchService;
import com.kukuxer.registration.service.interfaces.UserService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final UserService userService;
    private final RequestService requestService;
    private final SearchService searchService;


    @PostMapping("/search")
    public ResponseEntity<String> searchSomeoneToPlay() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userSender = userService.getByUsername(authentication.getName());
        SearchRequest searchRequest = searchService.checkIfSomeoneWaitingForMe(userSender.getId());
        if (searchRequest == null) {
            searchService.createRequest(userSender.getId());
            return ResponseEntity.ok("Looking for someone to play");
        } else {
            searchService.acceptRequest(userSender.getId(), searchRequest.getId());
            return ResponseEntity.ok("You found match!");
        }
    }


    @PostMapping("/send/{receiverId}")
    public ResponseEntity<String> sendRequest(@PathVariable("receiverId") long receiverId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userSender = userService.getByUsername(authentication.getName());

            if (userSender == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user  provided.");
            }

            requestService.createRequest(receiverId, userSender.getId());
            return ResponseEntity.ok("Request sent successfully.");

        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending request.");
        }
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<String> acceptRequest(@PathVariable("requestId") long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User userReceiver = userService.getByUsername(authentication.getName());
        if (userReceiver == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user provided.");
        }

        requestService.acceptRequest(requestId, userReceiver.getId());
        return ResponseEntity.ok("Request accept successfully.");
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectRequest(@PathVariable("requestId") long requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        requestService.rejectRequest(requestId, user.getId());
        return ResponseEntity.ok("Request rejected successfully.");
    }
}