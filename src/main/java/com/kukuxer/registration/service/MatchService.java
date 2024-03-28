package com.kukuxer.registration.service;

import org.springframework.http.ResponseEntity;

public interface MatchService {
    ResponseEntity<?> createMatch(long userId, long opponentId);
}
