package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.requests.FriendRequest;
import com.kukuxer.registration.domain.requests.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  RequestRepository extends JpaRepository<Request,Long> {
    List<Request> findAllByReceiverId(Long receiverId);
    List<Request> findAllBySenderId(Long senderId);
}

