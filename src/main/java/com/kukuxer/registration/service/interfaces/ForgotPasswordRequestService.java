package com.kukuxer.registration.service.interfaces;

import com.kukuxer.registration.domain.requests.ForgotPasswordRequest;
import com.kukuxer.registration.domain.user.User;

public interface ForgotPasswordRequestService {

    void createForgotPasswordRequest(User user);

    ForgotPasswordRequest getByToken(String token);

    boolean checkIfRequestLegal(ForgotPasswordRequest forgotPasswordRequest);

    void changePassword(ForgotPasswordRequest request, String newPassword);
}
