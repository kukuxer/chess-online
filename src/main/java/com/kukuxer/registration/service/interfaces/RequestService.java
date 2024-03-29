package com.kukuxer.registration.service.interfaces;


import com.kukuxer.registration.domain.request.Request;
import org.springframework.http.ResponseEntity;

public interface RequestService {

    Request createRequest(Long receiverId, Long senderId);

    ResponseEntity<?> acceptRequest(Long requestId, Long userId);

    ResponseEntity<?> rejectRequest(Long requestId, Long id);
}
