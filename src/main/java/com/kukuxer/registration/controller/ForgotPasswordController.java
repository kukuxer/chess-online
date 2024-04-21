package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.service.EmailService;
import com.kukuxer.registration.service.interfaces.ForgotPasswordRequestService;
import com.kukuxer.registration.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final EmailService emailService;
    private final ForgotPasswordRequestService forgotPasswordRequestService;
    private final UserService userService;

    @PostMapping("/sendRecoverlinkToEmail")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
            User user = userService.getByEmail(email);
            forgotPasswordRequestService.createForgotPasswordRequest(user);
            return ResponseEntity.ok("email sended");
        }
    }

