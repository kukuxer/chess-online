package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.match.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchHistoryRepository extends JpaRepository<MatchHistory,Long> {

                         // find last move
    @Query("SELECT mh FROM MatchHistory mh WHERE mh.match.id = :matchId ORDER BY mh.moveNumber DESC LIMIT 1")
    MatchHistory findTopByMatchOrderByMoveNumberDesc(@Param("matchId") Long matchId);
}

 