package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.user.FriendRequest;
import com.kukuxer.registration.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    Optional<FriendRequest> findFriendRequestBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<FriendRequest> findAllByReceiverId(Long receiverId);
    List<FriendRequest> findAllBySenderId(Long receiverId);
}
