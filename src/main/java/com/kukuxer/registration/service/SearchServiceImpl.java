package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.request.SearchRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import com.kukuxer.registration.repository.MatchRepository;
import com.kukuxer.registration.repository.SearchRequestRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.repository.UserStatisticRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import com.kukuxer.registration.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchRequestRepository searchRequestRepository;

    private final UserStatisticRepository userStatisticRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;
    private final MatchRepository matchRepository;

    @Override
    public SearchRequest checkIfSomeoneWaitingForMe(Long userId) {
        Optional<SearchRequest> sr = searchRequestRepository.findBySenderIdAndIsWaiting(userId,true);
        if(sr.isPresent()) throw new RuntimeException("You are already searching for a game.");
        isPlayingRightNow(userId);
        User user = userRepository.findById(userId).orElseThrow();
        UserStatistic userStatistic = userStatisticRepository.findByUser(user).orElseThrow();
        int userRating = userStatistic.getRating();
        List<SearchRequest> waitingRequests = searchRequestRepository.findByMinRatingGreaterThanEqualAndMaxRatingLessThanEqualAndIsWaitingIsTrue(userRating - 200, userRating + 200);

        Optional<SearchRequest> mostSuitableRequest = waitingRequests.stream()
                .filter(request -> request.getMinRating() <= userRating && request.getMaxRating() >= userRating)
                .min((request1, request2) ->
                        Math.abs(request1.getMinRating() - userRating) - Math.abs(request2.getMinRating() - userRating));

        return mostSuitableRequest.orElse(null);
    }

    @Override
    public void acceptRequest(Long userId, UUID searchRequestId) {
        isPlayingRightNow(userId);
        SearchRequest searchRequest = searchRequestRepository.findById(searchRequestId).orElseThrow();
        searchRequest.setWaiting(false);
        matchService.createMatch(userId, searchRequest.getSender().getId());
        searchRequestRepository.save(searchRequest);
    }

    @Override
    public void createRequest(Long userId) {
        isPlayingRightNow(userId);
        User user = userRepository.findById(userId).orElseThrow();
        UserStatistic userStatistic = userStatisticRepository.findByUser(user).orElseThrow();
        SearchRequest searchRequest = SearchRequest.builder().
                sender(user).
                minRating(userStatistic.getRating() - 200).
                maxRating(userStatistic.getRating() + 200).
                createdAt(LocalDateTime.now()).
                isWaiting(true).
                build();
        searchRequestRepository.save(searchRequest);
    }
    @Override
    public void isPlayingRightNow(Long userId){
        List<Match> match = matchRepository.findBySenderOrReceiverAndEndTimeIsNull(userRepository.findById(userId).orElseThrow());
        if(!match.isEmpty()) throw new RuntimeException("You are in match. ");
    }
}
