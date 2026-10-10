package com.example.ecommerceelectroniccomponentsbackend.config;

import com.example.ecommerceelectroniccomponentsbackend.service.IJwtService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtDecoderConfig implements JwtDecoder {
    private final IJwtService jwtService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;
    @Value("${app.jwt.secret-key}")
    private String secretKey;

    @Override
    public Jwt decode(String token) throws JwtException {
        log.info("Decoding JWT Token");
        try {
            if (!jwtService.verifyToken(token)) {
                throw new JwtException("Invalid token");
            }

            if (Objects.isNull(nimbusJwtDecoder)) {
                SecretKey secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HS512");
                nimbusJwtDecoder = NimbusJwtDecoder
                        .withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.HS512)
                        .build();
            }
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Failed to parse JWT token", e);
        }
        return nimbusJwtDecoder.decode(token);
    }


}
