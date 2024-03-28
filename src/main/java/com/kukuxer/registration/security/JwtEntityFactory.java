package com.kukuxer.registration.security;


import com.kukuxer.registration.domain.user.User;

public class JwtEntityFactory {

    public static JwtEntity create(User user){
        return new JwtEntity(
                user.getId(), user.getUsername(), user.getEmail(), user.getPassword());
    }

}
