package com.example.rentalTool_BackEnd.user.service;

import com.example.rentalTool_BackEnd.user.model.EmailVerificationToken;
import com.example.rentalTool_BackEnd.user.model.User;

import java.util.Optional;

public interface EmailVerificationService {
    EmailVerificationToken generateVerificationToken(User user);
    Optional<EmailVerificationToken> getToken(String token);
    void verifyEmail(String token);
    void deleteToken(Long userId);
    void resendVerificationEmail(String email);
}



