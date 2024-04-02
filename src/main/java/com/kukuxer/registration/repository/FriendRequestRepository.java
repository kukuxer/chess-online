package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.user.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest,Long> {
}
