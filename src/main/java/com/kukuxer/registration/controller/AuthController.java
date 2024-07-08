package com.kukuxer.registration.controller;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.dto.UserDTO;
import com.kukuxer.registration.dto.mappers.UserMapper;

import com.kukuxer.registration.security.JwtRequest;
import com.kukuxer.registration.security.JwtResponse;
import com.kukuxer.registration.service.UserServiceImpl;
import com.kukuxer.registration.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
//@CrossOrigin("http://localhost:4000")
public class AuthController {

    private final AuthService authService;
    private final UserServiceImpl userService;
    private final UserMapper userMapper;


    @PostMapping("/login")
    public JwtResponse login(@Validated @RequestBody JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public UserDTO register(@Validated @RequestBody UserDTO userDto) {
        User createdUser = userMapper.toEntity(userDto);
        userService.create(createdUser);
        return userMapper.toDto(createdUser);
    }

    @GetMapping("/checkUsername")
    public boolean checkUsername(@RequestParam String username) {
        try {
            return !isNull(userService.getByUsername(username));
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/checkEmail")
    public boolean checkEmail(@RequestParam String email) {
        try {
            return !isNull(userService.getByEmail(email));
        } catch (Exception e) {
            return false;
        }
    }
}
