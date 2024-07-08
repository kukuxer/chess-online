package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.service.UserServiceImpl;
import com.kukuxer.registration.service.interfaces.ForgotPasswordRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordRequestService forgotPasswordRequestService;
    private final UserServiceImpl userService;

    @PostMapping("/sendRecoverlinkToEmail")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");
        User user = userService.getByEmail(email);
        forgotPasswordRequestService.createForgotPasswordRequest(user, request.getRemoteAddr());
        return ResponseEntity.ok("email sended");
    }

    @PostMapping("/checkToken")
    public boolean checkToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        try {
            ForgotPasswordRequest request = forgotPasswordRequestService.getByToken(token);
            return forgotPasswordRequestService.checkIfRequestLegal(request);
        } catch (Exception e) {
            return false;
        }
    }


    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        String token = payload.get("token");

        ForgotPasswordRequest request = forgotPasswordRequestService.getByToken(token);
        if (forgotPasswordRequestService.checkIfRequestLegal(request)) {
            forgotPasswordRequestService.changePassword(request, newPassword);
            return ResponseEntity.ok("password successfully changed ");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is not active");
    }
}

