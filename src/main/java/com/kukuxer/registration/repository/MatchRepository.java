package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match,Long> {
}
