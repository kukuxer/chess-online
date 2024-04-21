package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRequestRepository extends JpaRepository<ForgotPasswordRequest, Long> {
    Optional<ForgotPasswordRequest> findByToken(String token);
}
