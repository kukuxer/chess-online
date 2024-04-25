package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ForgotPasswordRequestRepository extends JpaRepository<ForgotPasswordRequest, Long> {
    Optional<ForgotPasswordRequest> findByToken(String token);
    @Query("SELECT fpr FROM ForgotPasswordRequest fpr " +
            "WHERE fpr.ipAddress = :ipAddress " +
            "AND fpr.createdAt <= CURRENT_TIMESTAMP " +
            "ORDER BY fpr.createdAt DESC")
    Optional<ForgotPasswordRequest> findNearestByIpAddress(@Param("ipAddress") String ipAddress);
}

