package com.example.rentalTool_BackEnd.user.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token, String firstName);
}



