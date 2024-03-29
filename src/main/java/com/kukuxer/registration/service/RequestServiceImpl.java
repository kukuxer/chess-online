package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.request.Request;
import com.kukuxer.registration.domain.request.Status;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.RequestRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {


    private final MatchService matchService;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public Request createRequest(Long receiverId, Long senderId) {
        if (receiverId == senderId) throw new RuntimeException("mn");
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new RuntimeException("Receiver not found.")
        );
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new RuntimeException("Sender not found")
        );

        Request request = Request.builder()
                .sender(sender)
                .receiver(receiver)
                .status(Status.PENDING)
                .build();
        requestRepository.save(request);
        return request;
    }

    @Override
    public ResponseEntity<?> acceptRequest(Long requestId, Long receiverId) {
        // Retrieve the request from the repository
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found."));

        // Ensure that the receiver ID matches and the request status is PENDING
        if (!isAcceptableRequest(request, receiverId)) {
            throw new RuntimeException("Request is not ready to accept");
        }

        // Update the request status to ACCEPTED
        request.setStatus(Status.ACCEPTED);
        requestRepository.save(request);

        // Create a match using the match service
        matchService.createMatch(request.getSender().getId(), receiverId);

        // Return a ResponseEntity indicating success
        return ResponseEntity.ok("Request accepted successfully.");
    }

    private boolean isAcceptableRequest(Request request, Long receiverId) {
        return request.getReceiver().getId().equals(receiverId) &&
                request.getStatus() == Status.PENDING;
    }

    @Override
    public ResponseEntity<?> rejectRequest(Long requestId, Long id) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request not found.")
        );
        if (!request.getReceiver().getId().equals(id) || request.getStatus() != (Status.PENDING)) {
            throw new RuntimeException("Request is not ready to reject");
        } else {
            request.setStatus(Status.REJECTED);
            requestRepository.save(request);
            return ResponseEntity.ok("Request rejected successfully.");
        }
    }
}
