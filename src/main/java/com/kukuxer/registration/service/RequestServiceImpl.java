package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.request.Request;
import com.kukuxer.registration.domain.request.Status;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.RequestRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import jakarta.persistence.EntityNotFoundException;
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
        if (receiverId.equals(senderId)) throw new RuntimeException("You cannot send request to yourself");
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
    public ResponseEntity<String> rejectRequest(Long requestId, Long userId) {
        // Find the request by ID
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with ID: " + requestId));

        // Ensure that the user is authorized to reject the request
        if (!request.getReceiver().getId().equals(userId) || request.getStatus() != Status.PENDING) {
            throw new IllegalStateException("You cannot reject this request.");
        }

        // Update the request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestRepository.save(request);

        // Return a response entity with a success message
        return ResponseEntity.ok("Request rejected successfully.");
    }
}
