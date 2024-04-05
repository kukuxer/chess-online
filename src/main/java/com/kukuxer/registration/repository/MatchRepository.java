package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match,Long> {
    @Query("SELECT m FROM Match m WHERE m.sender = :user OR m.receiver = :user")
    List<Match> findAllByReceiverOrSender(@Param("user") User user);

    @Query("SELECT m FROM Match m WHERE m.sender = :user OR m.receiver = :user AND m.endTime IS NULL")
    List<Match> findBySenderOrReceiverAndEndTimeIsNull(User user);
}
