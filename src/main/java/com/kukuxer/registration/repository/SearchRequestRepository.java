package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.request.SearchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchRequestRepository extends JpaRepository<SearchRequest, UUID> {
    @Query(value = "SELECT * FROM search_request WHERE min_opponent_rating >= :minRating AND max_opponent_rating <= :maxRating AND is_waiting = true;",nativeQuery = true)
    List<SearchRequest> findByMinRatingGreaterThanEqualAndMaxRatingLessThanEqualAndIsWaitingIsTrue(int minRating, int maxRating);

    Optional<SearchRequest> findBySenderIdAndIsWaiting(Long sender_id, boolean isWaiting);


}
