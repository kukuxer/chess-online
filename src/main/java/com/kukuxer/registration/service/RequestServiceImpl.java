package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.request.Request;
import com.kukuxer.registration.domain.request.Status;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.RequestRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.MatchService;
import com.kukuxer.registration.service.interfaces.RequestService;
import com.kukuxer.registration.service.interfaces.SearchService;
import com.kukuxer.registration.service.interfaces.UserService;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {


    private final MatchService matchService;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SearchService searchService;
    @Override
    public Request createRequest(Long receiverId, Long senderId) {
        searchService.isPlayingRightNow(senderId);
        try {
            if (receiverId.equals(senderId)) {
                throw new IllegalArgumentException("You cannot send a request to yourself.");
            }

            User receiver = userRepository.findById(receiverId).orElseThrow(
                    () -> new EntityNotFoundException("Receiver not found with ID: " + receiverId)
            );

            User sender = userRepository.findById(senderId).orElseThrow(
                    () -> new EntityNotFoundException("Sender not found with ID: " + senderId)
            );

            Request request = Request.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .status(Status.PENDING)
                    .build();
            requestRepository.save(request);

            return request;
        } catch (IllegalArgumentException | EntityNotFoundException ex) {
            Sentry.captureException(ex);
            throw ex;
        } catch (Exception ex) {
            Sentry.captureException(ex);
            throw new RuntimeException("Failed to create request due to an unexpected error.");
        }
    }

    @Override
    public ResponseEntity<?> acceptRequest(Long requestId, Long receiverId) {
        searchService.isPlayingRightNow(receiverId);
        try {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("Request not found with ID: " + requestId));

            // Ensure that the receiver ID matches and the request status is PENDING
            if (!isAcceptableRequest(request, receiverId)) {
                throw new IllegalStateException("Request is not ready to accept");
            }
            searchService.isPlayingRightNow(request.getSender().getId());
            userService.updateUsersInGame(request.getSender(),request.getReceiver());
            request.setStatus(Status.ACCEPTED);

            requestRepository.save(request);

            matchService.createMatch(request.getSender().getId(), receiverId);

            return ResponseEntity.ok("Request accepted successfully.");
        } catch (EntityNotFoundException e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.", e);
        } catch (IllegalStateException e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not ready to accept", e);
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    private boolean isAcceptableRequest(Request request, Long receiverId) {
        return request.getReceiver().getId().equals(receiverId) &&
                request.getStatus() == Status.PENDING;
    }

    @Override
    public ResponseEntity<String> rejectRequest(Long requestId, Long userId) {
        try {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("Request not found with ID: " + requestId));

            if (!request.getReceiver().getId().equals(userId) || request.getStatus() != Status.PENDING) {
                throw new RequestRejectedException("You cannot reject this request.");
            }

            request.setStatus(Status.REJECTED);
            requestRepository.save(request);

            return ResponseEntity.ok("Request rejected successfully.");
        } catch (EntityNotFoundException e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.", e);
        } catch (IllegalStateException e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage(), e);
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }
}
