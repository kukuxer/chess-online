package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.match.Board;
import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MatchService {



    List<Match> getAllMatchesByUser(User user);

    ResponseEntity<?> createMatch(long userId, long opponentId);

    Board getBoard(long matchId);

    ResponseEntity<?> makeMove(long matchId, User user, String board,int finishResult);

    User getOpponent(long userId);

    Match getById(long matchId);


    boolean isInTheGameRightNow(Long userId);


    @SneakyThrows
    Match getCurrentMatchByUser(User user);
}
