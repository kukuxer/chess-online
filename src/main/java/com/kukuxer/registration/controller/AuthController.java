package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.dto.UserDTO;
import com.kukuxer.registration.dto.mappers.UserMapper;

import com.kukuxer.registration.security.JwtRequest;
import com.kukuxer.registration.security.JwtResponse;
import com.kukuxer.registration.service.interfaces.AuthService;
import com.kukuxer.registration.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
//@CrossOrigin("http://localhost:4000")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping("/login")
    public JwtResponse login(@Validated @RequestBody JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public UserDTO register(@Validated @RequestBody UserDTO userDto) {
        User createdUser = userMapper.toEntity(userDto);
        ;
        userService.create(createdUser);
        return userMapper.toDto(createdUser);
    }

    @GetMapping("/checkUsername")
    public boolean checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        try {
            return !isNull(userService.getByUsername(username));
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/checkEmail")
    public boolean checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            return !isNull(userService.getByEmail(email));
        } catch (Exception e) {
            return false;
        }
    }
}
