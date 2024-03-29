package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MatchService {



    List<Match> getAllMatchesByUser(User user);

    ResponseEntity<?> createMatch(long userId, long opponentId);
}
