package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.user.User;

public interface UserStatistics {

    void updateWinRating(Long matchId, User winner);
    void updateStalemateRating(Long matchId);

}
