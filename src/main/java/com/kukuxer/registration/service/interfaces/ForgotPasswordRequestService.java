package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.user.User;

public interface ForgotPasswordRequestService {

    void createForgotPasswordRequest(User user);
}
