package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.request.ForgotPasswordRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.LoginRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.ResetPasswordRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.VerifyEmailRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.ForgotPasswordResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.LoginResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.ResetPasswordResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.VerifyEmailResponse;

import java.text.ParseException;


public interface IAuthenticationService {
    LoginResponse login(LoginRequest request);
    void logout(String accessToken, String refreshToken) throws ParseException;
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
    VerifyEmailResponse verifyEmail(VerifyEmailRequest request);
    void sendVerificationEmail(String email);

}