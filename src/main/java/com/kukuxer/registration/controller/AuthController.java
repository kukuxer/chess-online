package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.dto.UserDTO;
import com.kukuxer.registration.dto.mappers.UserMapper;

import com.kukuxer.registration.security.JwtRequest;
import com.kukuxer.registration.security.JwtResponse;
import com.kukuxer.registration.service.AuthService;
import com.kukuxer.registration.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public JwtResponse login(@Validated @RequestBody JwtRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public UserDTO register(@Validated @RequestBody UserDTO userDto){
        User user = userMapper.toEntity(userDto);
        User createdUser = user;
        createdUser.setEmail(userDto.getEmail());
        userService.create(createdUser);
        return userMapper.toDto(createdUser);
    }
    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody String refreshToken){
        return authService.refresh(refreshToken);
    }
}
