package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.request.SearchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchRequestRepository extends JpaRepository<SearchRequest, UUID> {
    List<SearchRequest> findByMinRatingGreaterThanAndMaxRatingLessThanAndWaitingIsTrue(int minRating, int maxRating);
}
