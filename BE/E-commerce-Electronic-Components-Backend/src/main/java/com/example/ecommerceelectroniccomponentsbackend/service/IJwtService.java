package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.JwtInfo;
import com.example.ecommerceelectroniccomponentsbackend.dto.TokenPayload;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface IJwtService {

    TokenPayload generateAccessToken(User user);

    TokenPayload generateRefreshToken(User user);

    boolean verifyToken(String token) throws ParseException, JOSEException;

    JwtInfo parseToken(String token) throws ParseException;
}

