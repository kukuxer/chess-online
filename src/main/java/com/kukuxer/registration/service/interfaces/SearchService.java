package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.request.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;


public interface SearchService {
    SearchRequest checkIfSomeoneWaitingForMe(Long id);

    void acceptRequest(Long userId, UUID SearchRequestId);

    void createRequest(Long userId);

    void isPlayingRightNow(Long userId);
}
