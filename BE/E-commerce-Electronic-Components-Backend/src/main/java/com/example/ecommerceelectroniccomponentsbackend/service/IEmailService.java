package com.example.ecommerceelectroniccomponentsbackend.service;

public interface IEmailService {

    void sendVerificationEmail(String toEmail, String token);

    void sendPasswordResetEmail(String toEmail, String token);
}

