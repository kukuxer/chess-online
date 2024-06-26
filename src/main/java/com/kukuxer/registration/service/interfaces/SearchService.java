package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.requests.SearchRequest;
import com.kukuxer.registration.domain.user.User;

import java.util.UUID;


public interface SearchService {
    SearchRequest checkIfSomeoneWaitingForMe(Long id);

    void acceptRequest(Long userId, UUID SearchRequestId);

    void createRequest(Long userId);

    void stopSearching(SearchRequest searchRequest);

    void isPlayingRightNow(Long userId);

    SearchRequest getCurrentSearch(User user);
}
