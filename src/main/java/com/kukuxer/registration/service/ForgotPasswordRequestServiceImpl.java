package com.kukuxer.registration.service;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.repository.ForgotPasswordRequestRepository;
import com.kukuxer.registration.service.interfaces.ForgotPasswordRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForgotPasswordRequestServiceImpl implements ForgotPasswordRequestService {

    private final ForgotPasswordRequestRepository forgotPasswordRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void createForgotPasswordRequest(User user) {
        String token = generateToken();
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().
                token(passwordEncoder.encode(token)).
                createdAt(LocalDateTime.now()).
                user(user).
                isActive(true).
                build();
        forgotPasswordRequestRepository.save(request);
        // change after publishing
        String changePasswordUrl = "http://localhost:8080/forgotPassword/changePassword?token=" + token;
        emailService.sendMailRecoverPasswordTo(user.getEmail(),changePasswordUrl);
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
