package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.domain.user.UserStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticRepository extends JpaRepository<UserStatistic,Long> {
    Optional<UserStatistic> findByUser(User user);

}
