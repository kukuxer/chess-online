package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.requests.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRequestRepository extends JpaRepository<ForgotPasswordRequest, Long> {

}
