package com.kukuxer.registration.controller;


import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.requests.SearchRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.service.UserServiceImpl;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import com.kukuxer.registration.service.interfaces.SearchService;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final UserServiceImpl userService;
    private final RequestService requestService;
    private final SearchService searchService;
    private final MatchService matchService;


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

    @PostMapping("/stopSearching")
    public ResponseEntity<String> stopSearching() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userSender = userService.getByUsername(authentication.getName());
        SearchRequest searchRequest = searchService.getCurrentSearch(userSender);
        if (searchRequest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You are not looking for a game right now");
        } else {
            searchService.stopSearching(searchRequest);
            return ResponseEntity.ok("You stop searching for a match!");
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

    @GetMapping("/checkStatus")
    public ResponseEntity<?> CheckStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());

        try {
            Match match = matchService.getCurrentMatchByUser(user);
            if (match == null) return ResponseEntity.status(HttpStatus.CONFLICT).body("No match found for user.");
            return ResponseEntity.ok(match);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(409).body("No match found for user.");
        }
    }
}