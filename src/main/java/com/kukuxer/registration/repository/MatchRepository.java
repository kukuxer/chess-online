package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match,Long> {
    @Query("SELECT m FROM Match m WHERE m.sender = :user OR m.receiver = :user")
    List<Match> findAllByReceiverOrSender(@Param("user") User user);

}
