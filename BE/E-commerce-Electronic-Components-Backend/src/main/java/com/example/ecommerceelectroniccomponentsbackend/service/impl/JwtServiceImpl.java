package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.JwtInfo;
import com.example.ecommerceelectroniccomponentsbackend.dto.TokenPayload;
import com.example.ecommerceelectroniccomponentsbackend.model.BlacklistedToken;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.repository.BlacklistedTokenRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IJwtService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements IJwtService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    @Value("${app.jwt.secret-key}")
    private String secretKey;

    public TokenPayload generateAccessToken(User user) {

        //header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();

        Date expirationTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.MINUTES));
        String jwtId = UUID.randomUUID().toString();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))  // Use user ID as subject
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(jwtId)
                .claim("email", user.getEmail())  // Store email as claim
                .claim("role", user.getRole().name())
                .claim("scope", user.getRole().name())
                .build();

        //payload
        Payload payload = new Payload(claimsSet.toJSONObject());

        //Create key
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new RuntimeException("Error while signing the token", e);
        }
        String token = jwsObject.serialize();
        return TokenPayload.builder()
                .token(token)
                .jwtId(jwtId)
                .expiredTime(expirationTime)
                .build();
    }

    public TokenPayload generateRefreshToken(User user) {

        //header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();

        Date expirationTime = Date.from(issueTime.toInstant().plus(14, ChronoUnit.DAYS));
        String jwtId = UUID.randomUUID().toString();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(jwtId)
                .build();

        //payload
        Payload payload = new Payload(claimsSet.toJSONObject());

        //Create key
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new RuntimeException("Error while signing the token", e);
        }
        String token = jwsObject.serialize();
        return TokenPayload.builder()
                .token(token)
                .jwtId(jwtId)
                .expiredTime(expirationTime)
                .build();
    }


    public boolean verifyToken(String token) throws ParseException, JOSEException {
        // Kiem tra hieu luc cua token
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime.before(new Date())) {
            return false;
        }
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Optional<BlacklistedToken> byId = blacklistedTokenRepository.findById(jwtId);
        if (byId.isPresent()) {
            return false;
        }
        return signedJWT.verify(new MACVerifier(secretKey));
    }

    public JwtInfo parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        return JwtInfo.builder()
                .jwtId(jwtId)
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .build();
    }
}
