package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.ForgotPasswordRequestRepository;
import com.kukuxer.registration.repository.UserRepository;
import com.kukuxer.registration.service.interfaces.ForgotPasswordRequestService;
import io.sentry.Sentry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ForgotPasswordRequestServiceImpl implements ForgotPasswordRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordRequestServiceImpl.class);
    private final ForgotPasswordRequestRepository forgotPasswordRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void createForgotPasswordRequest(User user) {
        String token = generateToken();
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().
                token(token).
                createdAt(LocalDateTime.now()).
                user(user).
                isActive(true).
                build();
        forgotPasswordRequestRepository.save(request);
        // change after publishing
        String changePasswordUrl = "http://localhost:8080/forgotPassword/changePassword?token=" + token;
        emailService.sendMailRecoverPasswordTo(user.getEmail(), changePasswordUrl, user.getUsername());
    }

    @Override
    public ForgotPasswordRequest getByToken(String token) {
        try {
            ForgotPasswordRequest forgotPasswordRequest = forgotPasswordRequestRepository.findByToken(
                    (token)).orElseThrow(
                    () -> new EntityNotFoundException("User not found with token: " + token)
            );
            return forgotPasswordRequest;
        } catch (EntityNotFoundException e) {
            logger.error("Failed to retrieve user by token {}: {}", token, e.getMessage());
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by token {}", token, e);
            Sentry.captureException(e);
            throw new RuntimeException("Failed to retrieve ForgotPasswordRequest. Please try again later.");
        }
    }

    @Override
    public boolean checkIfRequestLegal(ForgotPasswordRequest forgotPasswordRequest) {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
        if (forgotPasswordRequest.isActive()) {
            if (forgotPasswordRequest.getCreatedAt().isAfter(fifteenMinutesAgo)) {
                return true;
            }
        }
        forgotPasswordRequest.setActive(false);
        forgotPasswordRequestRepository.save(forgotPasswordRequest);
        return false;
    }

    @Override
    public void changePassword(ForgotPasswordRequest request, String newPassword) {
        User user = request.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        request.setActive(false);
        userRepository.save(user);
        forgotPasswordRequestRepository.save(request);
    }


    private static String generateToken() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder token = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 15; i++) {
            int randomIndex = random.nextInt(characters.length());
            token.append(characters.charAt(randomIndex));
        }

        return "abobaToken" + token.toString();
    }
}
