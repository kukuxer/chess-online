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
                                      List<Integer> from, List<Integer> to,
                                      int finishResult) {
        try {
            if (from == null || to == null || from.size() != 2 || to.size() != 2) {
                throw new RuntimeException("Incorrect format for 'from' or 'to' coordinates");
            }

            MatchHistory lastMove = matchHistoryRepository.findTopByMatchOrderByMoveNumberDesc(matchId);
            if (lastMove == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Match not found");
            }
            // aboba

            MatchHistory newMove = new MatchHistory();
            newMove.setMatch(lastMove.getMatch());
            newMove.setMoveNumber(lastMove.getMoveNumber() + 1);
            newMove.setMoveTimestamp(LocalDateTime.now());
            newMove.setUser(user);

            String lastBoardString = lastMove.getBoard();
            int[][] lastBoard = convertStringToIntMatrixChessboard(lastBoardString);
            // y,x
            lastBoard[to.get(0)][to.get(1)] = lastBoard[from.get(0)][from.get(1)];
            lastBoard[from.get(0)][from.get(1)] = 0;
            newMove.setBoard(convertChessBoardToJson(lastBoard));
            matchHistoryRepository.save(newMove);

            switch (finishResult) {
                case 0:
                    break;
                case 1:
                    win(matchId, user);
                    break;
                case 2:
                    stalemate(matchId);
                    break;
                default:
                    throw new RuntimeException("wrong finish result!");
            }
            return ResponseEntity.ok("Move successful");
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Error occurred while making the move.");
        }
    }

    private void win(Long matchId, User winner) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));
        match.setWinner(winner);
        match.setEndTime(LocalDateTime.now());

        // Get loser and loser's statistic
        User loser = match.getSender().equals(winner) ? match.getReceiver() : match.getSender();
        UserStatistic loserStatistic = userStatisticRepository.findByUser(loser)
                .orElseThrow(() -> new RuntimeException("User statistic not found with user: " + loser));
        loserStatistic.setTotalGamesPlayed(loserStatistic.getTotalGamesPlayed() + 1);
        loserStatistic.setLosses(loserStatistic.getLosses() + 1);

        // Get winner's statistic and update
        UserStatistic userStatistic = userStatisticRepository.findByUser(winner)
                .orElseThrow(() -> new RuntimeException("User statistic not found with user: " + winner));
        userStatistic.setTotalGamesPlayed(userStatistic.getTotalGamesPlayed() + 1);
        userStatistic.setWins(userStatistic.getWins() + 1);

        // Update winner's rating
        int winnerRating = calculateNewRating(
                userStatistic.getRating(),
                loserStatistic.getRating(),
                1,
                userStatistic.getConfidence());

        // Update loser's rating
        int loserRating = calculateNewRating(
                loserStatistic.getRating(),
                userStatistic.getRating(),
                0,
                loserStatistic.getConfidence());

        // Save updated statistics
        userStatistic.setRating(winnerRating);
        userStatistic.setConfidence(
                updateConfidence(userStatistic.getTotalGamesPlayed(),
                        userStatistic.getRating()
                )
        );
        userStatisticRepository.save(userStatistic);

        loserStatistic.setRating(loserRating);
        loserStatistic.setConfidence(
                updateConfidence(loserStatistic.getTotalGamesPlayed(),
                        loserStatistic.getRating()
                )
        );
        userStatisticRepository.save(loserStatistic);
    }


    private void stalemate(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));

        match.setEndTime(LocalDateTime.now());
        matchRepository.save(match);

        // Update statistics for the first player
        User firstPlayer = match.getSender();
        UserStatistic firstPlayerStatistic = userStatisticRepository.findByUser(firstPlayer)
                .orElseThrow(() -> new RuntimeException("User statistic not found with user: " + firstPlayer));
        firstPlayerStatistic.setTotalGamesPlayed(firstPlayerStatistic.getTotalGamesPlayed() + 1);
        firstPlayerStatistic.setDraws(firstPlayerStatistic.getDraws() + 1);
        firstPlayerStatistic.setRating(calculateNewRating(
                        firstPlayerStatistic.getRating(),
                        firstPlayerStatistic.getRating(),
                        0.5,
                firstPlayerStatistic.getConfidence()
                )
        );
        firstPlayerStatistic.setConfidence(updateConfidence(
                firstPlayerStatistic.getTotalGamesPlayed(),
                firstPlayerStatistic.getRating())
        );
        userStatisticRepository.save(firstPlayerStatistic);

        // Update statistics for the second player
        User secondPlayer = match.getReceiver();
        UserStatistic secondPlayerStatistic = userStatisticRepository.findByUser(secondPlayer)
                .orElseThrow(() -> new RuntimeException("User statistic not found with user: " + secondPlayer));
        secondPlayerStatistic.setTotalGamesPlayed(secondPlayerStatistic.getTotalGamesPlayed() + 1);
        secondPlayerStatistic.setDraws(secondPlayerStatistic.getDraws() + 1);
        secondPlayerStatistic.setRating(calculateNewRating(
                        secondPlayerStatistic.getRating(),
                        secondPlayerStatistic.getRating(),
                        0.5,
                secondPlayerStatistic.getConfidence()
                )
        );
        secondPlayerStatistic.setConfidence(updateConfidence(
                secondPlayerStatistic.getTotalGamesPlayed(),
                secondPlayerStatistic.getRating())
        );
        userStatisticRepository.save(secondPlayerStatistic);
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

    //If the player wins the game, the score would be 1.
    //If the game ends in a draw, the score would be 0.5.
    //If the player loses the game, the score would be 0.
    public int calculateNewRating(int playerRating, int opponentRating, double score, double confidence) {
        double expectedScore = calculateExpectedScore(playerRating, opponentRating);
        int ratingChange = (int) Math.ceil(confidence * (score - expectedScore));
        return playerRating + ratingChange;
    }

    // Adjust the K-factor as needed based on your requirements

    private double calculateExpectedScore(int playerRating, int opponentRating) {
        return (1.0 / (1.0 + Math.pow(10.0, (opponentRating - playerRating) / 400.0)));
    }

    public double updateConfidence(int totalGamesPlayed, int rating) {
        int k;

        if (totalGamesPlayed < 30) {
            k = 115 - totalGamesPlayed;
        } else {
            if (rating >= 2400) {
                k = 20;
            } else if (rating >= 2000) {
                k = 30;
            } else if (rating >= 1500) {
                k = 40;
            } else if (rating >= 500) {
                k = 60;
            } else {
                k = 70;
            }
        }
        return k;
    }
}
