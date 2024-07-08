package com.kukuxer.registration.service.interfaces;


import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.requests.Request;
import com.kukuxer.registration.domain.user.User;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface RequestService {

    Request createRequest(Long receiverId, Long senderId);

    ResponseEntity<?> acceptRequest(Long requestId, Long userId);

    boolean isAcceptableRequest(Request request, Long receiverId);

    ResponseEntity<?> rejectRequest(Long requestId, Long id);


}
