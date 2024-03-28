package com.kukuxer.registration.security;

import lombok.Data;

@Data
public class JwtResponse {

    private Long id;
    private String username;
    private String accessToken;
    private String refreshToken;

}
