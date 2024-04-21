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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
//@CrossOrigin("http://localhost:4000")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping("/login")
    public JwtResponse login(@Validated @RequestBody JwtRequest loginRequest){
        return authService.login(loginRequest);
    }
    // test



    @PostMapping("/register")
    public UserDTO register(@Validated @RequestBody UserDTO userDto){
        User createdUser = userMapper.toEntity(userDto);;
        userService.create(createdUser);
        return userMapper.toDto(createdUser);
    }
    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody String refreshToken){
        return authService.refresh(refreshToken);
    }
}
