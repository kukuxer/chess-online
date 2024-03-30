package com.kukuxer.registration.service;

import com.google.gson.Gson;
import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.match.MatchHistory;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.MatchHistoryRepository;
import com.kukuxer.registration.repository.MatchRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final UserRepository userRepository;


    @Override
    public List<Match> getAllMatchesByUser(User user) {
        return matchRepository.findAllByReceiverOrSender(user);
    }

    @Override
    public ResponseEntity<?> createMatch(long senderId, long receiverId) {

        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new RuntimeException("Sender not found.")
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new RuntimeException("Receiver not found.")
        );
        Match match = new Match();
        match.setSender(sender);
        match.setReceiver(receiver);
        match.setStartTime(LocalDateTime.now());
        // randomly sets white player
        Random random = new Random();
        if (random.nextBoolean()) {
            match.setWhiteId(sender);
        } else {
            match.setWhiteId(receiver);
        }

        matchRepository.save(match);

        MatchHistory matchHistory = new MatchHistory();
        matchHistory.setMatch(match);
        matchHistory.setBoard(createBoard());
        matchHistoryRepository.save(matchHistory);

        return ResponseEntity.ok("Match created successfully.");

    }

    private String createBoard() {
/*
        1: White Pawn
        3: White Knight
        4: White Bishop
        5: White Rook
        6: White Queen
        7: White King
        -1: Black Pawn
        -3: Black Bishop
        -4: Black Knight
        -5: Black Rook
        -6: Black Queen
        -7: Black King
*/
        int[][] chessBoard = {
                {-5, -3, -4, -6, -7, -4, -3, -5}, // Row 1 (Black Pieces)
                {-1, -1, -1, -1, -1, -1, -1, -1}, // Row 2 (Black Pawns)
                { 0,  0,  0,  0,  0,  0,  0,  0}, // Row 3 (Empty Squares)
                { 0,  0,  0,  0,  0,  0,  0,  0}, // Row 4 (Empty Squares)
                { 0,  0,  0,  0,  0,  0,  0,  0}, // Row 5 (Empty Squares)
                { 0,  0,  0,  0,  0,  0,  0,  0}, // Row 6 (Empty Squares)
                { 1,  1,  1,  1,  1,  1,  1,  1}, // Row 7 (White Pawns)
                { 5,  3,  4,  6,  7,  4,  3,  5}  // Row 8 (White Pieces)
        };
        String json = convertChessBoardToJson(chessBoard);
        System.out.println(json);
        return json;
    }
    public static String convertChessBoardToJson(int[][] chessBoard) {
        Gson gson = new Gson();
        return gson.toJson(chessBoard);
    }
}