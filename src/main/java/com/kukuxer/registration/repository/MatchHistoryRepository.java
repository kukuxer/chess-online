package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.match.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchHistoryRepository extends JpaRepository<MatchHistory,Long> {
}
