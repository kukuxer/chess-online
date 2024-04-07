package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.MoveRequest;
import com.kukuxer.registration.domain.match.Board;
import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.SearchService;
import com.kukuxer.registration.service.interfaces.UserService;
import com.kukuxer.registration.service.interfaces.UserStatistics;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final UserService userService;
    private final MatchService matchService;
    private final UserStatistics userStatistics;


    @GetMapping("/getBoard/{matchId}")
    public Board getBoard(@PathVariable("matchId") long matchId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.getByUsername(authentication.getName());
            if (user == null) {
                throw new RuntimeException("Invalid user provided.");
            }

            return matchService.getBoard(matchId);
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("We cannot retrieve the board");
        }
    }

    @PostMapping("/move/{matchId}")
    public ResponseEntity<?> makeMove(@PathVariable("matchId") long matchId,
                                      @RequestBody MoveRequest moveRequest) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.getByUsername(authentication.getName());
            if (user == null) {
                throw new RuntimeException("Invalid user provided.");
            }
            matchService.makeMove(matchId, user, moveRequest.getBoard(), moveRequest.getFinishResult());
            return ResponseEntity.ok("You moved successfully");

    }
    @PostMapping("/resign/{matchId}")
    public ResponseEntity<?> resign(@PathVariable("matchId") long matchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getByUsername(authentication.getName());
        if (user == null) {throw new RuntimeException("Invalid user provided.");}
        if(matchService.isInTheGameRightNow(user.getId())){
            userStatistics.updateWinRating(matchId,matchService.getOpponent(user.getId()));
        } else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot resign");
        }
        return ResponseEntity.ok("You resigned!");
    }
}
