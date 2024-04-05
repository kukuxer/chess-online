package com.kukuxer.registration.service;

import com.google.gson.Gson;
import com.kukuxer.registration.domain.match.Board;
import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.match.MatchHistory;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.MatchHistoryRepository;
import com.kukuxer.registration.repository.MatchRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.repository.UserStatisticRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.UserStatistics;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final UserRepository userRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final UserStatistics userStatistics;

    @Override
    public List<Match> getAllMatchesByUser(User user) {
        return matchRepository.findAllByReceiverOrSender(user);
    }

    @Override
    public ResponseEntity<?> createMatch(long senderId, long receiverId) {
        try {
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
                match.setWhiteUser(sender);
            } else {
                match.setWhiteUser(receiver);
            }

            matchRepository.save(match);

            MatchHistory matchHistory = new MatchHistory();
            matchHistory.setMatch(match);
            matchHistory.setBoard(createBoard());
            matchHistoryRepository.save(matchHistory);

            return ResponseEntity.ok("Match created successfully.");
        } catch (Exception e) {
            // Log the exception to Sentry.io
            Sentry.captureException(e);
            // Return an appropriate error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create match. Please try again later.");
        }
    }

    @Override
    public Board getBoard(long matchId) {
        try {
            Match match = getById(matchId);
            MatchHistory matchHistory = matchHistoryRepository.findTopByMatchOrderByMoveNumberDesc(matchId);
            User whitePlayer = match.getWhiteUser();
            User blackPlayer = match.getBlack();

            return Board.builder()
                    .matchId(matchId)
                    .WhiteUsername(whitePlayer.getUsername())
                    .BlackUsername(blackPlayer.getUsername())
                    .lastMoveNumber(matchHistory.getMoveNumber())
                    .board(convertStringToIntMatrixChessboard(matchHistory.getBoard()))
                    .build();
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Error occurred while retrieving the board.");
        }
    }

    @Override
    public ResponseEntity<?> makeMove(long matchId,
                                      User user,
                                     String board,
                                      int finishResult) {
        Match match = matchRepository.findById(matchId).orElseThrow();
        if(match.getWinner()!=null){
            throw new RuntimeException("Match is finished.");
        }
        try {
            MatchHistory lastMove = matchHistoryRepository.findTopByMatchOrderByMoveNumberDesc(matchId);
            if (lastMove == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Match not found");
            }
            int moveNumber =lastMove.getMoveNumber();
            User currentUser;
            String color;
            if(moveNumber%2==0){
                currentUser= match.getWhiteUser();
                color = "white";
            }else{

                currentUser=match.getBlack();
                color = "black";
            }
            if(currentUser!=user){
                throw new RuntimeException("you are not "+color);
            }

            MatchHistory newMove = MatchHistory.builder()
                    .match(lastMove.getMatch())
                    .moveNumber(lastMove.getMoveNumber()+1)
                    .moveTimestamp(LocalDateTime.now())
                    .user(user)
                    .board(board)
                    .build();
            matchHistoryRepository.save(newMove);

            switch (finishResult) {
                case 0:
                    break;
                case 1:
                    userStatistics.updateWinRating(matchId,user);
                    break;
                case 2:
                    userStatistics.updateStalemateRating(matchId);
                    break;
                default:
                    throw new RuntimeException("wrong finish result!");
            }
            return ResponseEntity.ok("Move successful");
        }
        catch (RuntimeException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e.getMessage());
        }
        catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Error occurred while making the move.");
        }

    }

    @Override
    public Match getById(long matchId) {
        try {
            return matchRepository.findById(matchId)
                    .orElseThrow(() -> new EntityNotFoundException("Match not found with ID: " + matchId));
        } catch (EntityNotFoundException ex) {
            Sentry.captureException(ex);
            throw ex;
        }
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
                {0, 0, 0, 0, 0, 0, 0, 0}, // Row 3 (Empty Squares)
                {0, 0, 0, 0, 0, 0, 0, 0}, // Row 4 (Empty Squares)
                {0, 0, 0, 0, 0, 0, 0, 0}, // Row 5 (Empty Squares)
                {0, 0, 0, 0, 0, 0, 0, 0}, // Row 6 (Empty Squares)
                {1, 1, 1, 1, 1, 1, 1, 1}, // Row 7 (White Pawns)
                {5, 3, 4, 6, 7, 4, 3, 5}  // Row 8 (White Pieces)
        };
        String json = convertChessBoardToJson(chessBoard);
        System.out.println(json);
        return json;
    }

    public static String convertChessBoardToJson(int[][] chessBoard) {
        Gson gson = new Gson();
        return gson.toJson(chessBoard);
    }

    public static int[][] convertStringToIntMatrixChessboard(String chessboardString) {
        // Remove leading and trailing brackets
        chessboardString = chessboardString.substring(2, chessboardString.length() - 2);

        // Split the string into rows
        String[] rows = chessboardString.split("\\],\\[");
        // Initialize the chessboard array
        int[][] chessboard = new int[rows.length][];

        // Process each row
        for (int i = 0; i < rows.length; i++) {
            // Split the row into cells
            String[] cells = rows[i].split(",");

            // Initialize the row array
            chessboard[i] = new int[cells.length];

            // Process each cell
            for (int j = 0; j < cells.length; j++) {
                try {
                    // Parse the cell string to integer
                    chessboard[i][j] = Integer.parseInt(cells[j].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing cell [" + i + "][" + j + "]: " + e.getMessage());
                }
            }
        }
        return chessboard;
    }
}
