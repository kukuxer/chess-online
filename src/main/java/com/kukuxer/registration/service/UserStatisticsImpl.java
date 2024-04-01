package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.MatchRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.repository.UserStatisticRepository;
import com.kukuxer.registration.service.interfaces.UserService;
import com.kukuxer.registration.service.interfaces.UserStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatisticsImpl implements UserStatistics {

    private final MatchRepository matchRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    @Override
    public void updateWinRating(Long matchId, User winner) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));
        match.setWinner(winner);
        match.setEndTime(LocalDateTime.now());
        userService.updateUsersInGame(match.getBlack(),match.getWhiteUser());
        matchRepository.save(match);
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
    @Override
    public void updateStalemateRating(Long matchId) {
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
