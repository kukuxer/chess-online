package com.kukuxer.registration.service.interfaces;


import com.kukuxer.registration.domain.user.User;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional
    User create(User user);

    User getById(Long userId);

    User getByUsername(String username);

    void updateUsersInGame(User user1, User user2);

    void createStatistic(User user);
}
