package com.kukuxer.registration.service.interfaces;


import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.user.User;

import java.util.List;

public interface UserService {
    User create(User user);


    User getById(Long userId);

    User getByUsername(String username);

    User getByEmail(String email);

    void updateUsersInGame(User user1, User user2);

    void createStatistic(User user);

    FriendRequest sendFriendRequest(Long userId, Long senderId);

    void acceptFriendRequest(Long friendRequestId);
    void rejectFriendRequest(Long friendRequestId);
    FriendRequest findFriendRequestById(Long friendRequestId);
    List<FriendRequest> getAllByReceiverId(Long receiverId);
    List<FriendRequest> getAllBySenderId(Long senderId);

}
