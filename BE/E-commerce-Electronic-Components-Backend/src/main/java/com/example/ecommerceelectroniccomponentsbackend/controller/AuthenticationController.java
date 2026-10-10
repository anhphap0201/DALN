package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.request.*;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.*;
import com.example.ecommerceelectroniccomponentsbackend.service.IAuthenticationService;
import com.example.ecommerceelectroniccomponentsbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    private final IUserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = authenticationService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from(
                        "refresh_token",
                        loginResponse.getRefreshToken()
                )
                .httpOnly(true)
                .secure(false) // true nếu HTTPS
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Refresh token from cookie")
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) throws ParseException {

        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = authHeader.substring(7);
        authenticationService.logout(accessToken, refreshToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/change-password")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        ChangePasswordResponse response = userService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        ForgotPasswordResponse response = authenticationService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        ResetPasswordResponse response = authenticationService.resetPassword(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/auth/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        log.info("Email verification request received");
        VerifyEmailResponse response = authenticationService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/auth/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        log.info("Resend verification email request for: {}", email);
        authenticationService.sendVerificationEmail(email);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.");
        response.put("email", email);
        return ResponseEntity.ok(response);
    }

}
