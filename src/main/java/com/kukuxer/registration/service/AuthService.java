package com.kukuxer.registration.service;

import com.kukuxer.registration.security.JwtRequest;
import com.kukuxer.registration.security.JwtResponse;

public interface AuthService {
    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(String refreshToken);
}
