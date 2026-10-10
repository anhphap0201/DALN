package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.JwtInfo;
import com.example.ecommerceelectroniccomponentsbackend.dto.TokenPayload;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.ForgotPasswordRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.LoginRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.ResetPasswordRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.VerifyEmailRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.ForgotPasswordResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.LoginResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.ResetPasswordResponse;
import com.example.ecommerceelectroniccomponentsbackend.dto.response.VerifyEmailResponse;
import com.example.ecommerceelectroniccomponentsbackend.model.*;
import com.example.ecommerceelectroniccomponentsbackend.repository.*;
import com.example.ecommerceelectroniccomponentsbackend.service.IAuthenticationService;
import com.example.ecommerceelectroniccomponentsbackend.service.IEmailService;
import com.example.ecommerceelectroniccomponentsbackend.service.IJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final IJwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Override
    public LoginResponse login(LoginRequest request) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authenticate = authenticationManager.authenticate(token);

        // Trả về token
        User user = (User) authenticate.getPrincipal();
        TokenPayload accessPayload = jwtService.generateAccessToken(user);
        TokenPayload refreshPayload = jwtService.generateRefreshToken(user);
        long refreshTtl =
                refreshPayload.getExpiredTime().getTime() - System.currentTimeMillis();

        refreshTokenRepository.save(RefreshToken.builder()
                .jwtId(refreshPayload.getJwtId())
                .ttl(refreshTtl)
                .build()
        );


        return LoginResponse.builder()
                .accessToken(accessPayload.getToken())
                .refreshToken(refreshPayload.getToken())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
    @Override
    public void logout(String accessToken, String refreshToken) throws ParseException {
        JwtInfo accessInfo = jwtService.parseToken(accessToken);

        long ttl =
                accessInfo.getExpirationTime().getTime() - System.currentTimeMillis();
        log.info("TTL: {}", ttl);
        if (ttl > 0) {
            try {
                blacklistedTokenRepository.save(
                        BlacklistedToken.builder()
                                .jwtId(accessInfo.getJwtId())
                                .ttl(ttl)
                                .build()
                );
                log.info("Saved access token jwtId={} to Redis blacklist with ttl={}ms", accessInfo.getJwtId(), ttl);
            } catch (Exception e) {
                log.error("Failed to save blacklisted token jwtId={} to Redis: {}", accessInfo.getJwtId(), e.getMessage(), e);
                // Optional: rethrow or continue depending on desired behavior. Here we continue logout but log the error.
            }
        }

        JwtInfo refreshInfo = jwtService.parseToken(refreshToken);
        refreshTokenRepository.deleteById(refreshInfo.getJwtId());

        log.info("Logout success");
    }
    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User with this email does not exist"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();

        // Save token to Redis with 1 hour expiration
        long ttl = 60 * 60 * 1000;

        try {
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .token(resetToken)
                    .email(user.getEmail())
                    .ttl(ttl)
                    .build()
            );
            log.info("Password reset token saved to Redis");
        } catch (Exception e) {
            log.error("Failed to save token to Redis: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo token đặt lại mật khẩu. Vui lòng kiểm tra Redis service.");
        }

        // Send email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            try {
                passwordResetTokenRepository.deleteById(resetToken);
            } catch (Exception ex) {
                log.error("Failed to cleanup token: {}", ex.getMessage());
            }
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng kiểm tra cấu hình email.");
        }

        return ForgotPasswordResponse.builder()
                .message("Một email đặt lại mật khẩu đã được gửi đến địa chỉ email của bạn. Vui lòng kiểm tra hộp thư.")
                .email(user.getEmail())
                .build();
    }
    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request with token");

        // Validate token
        PasswordResetToken resetToken = passwordResetTokenRepository.findById(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn"));

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // Get user and update password
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete the used token
        passwordResetTokenRepository.delete(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());

        return ResetPasswordResponse.builder()
                .message("Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập với mật khẩu mới.")
                .email(user.getEmail())
                .build();
    }
    @Override
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        log.info("Email verification request with token");

        // Validate token
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findById(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn"));

        // Get user and verify email
        User user = userRepository.findByEmail(verificationToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEmailVerified() != null && user.getEmailVerified()) {
            throw new IllegalArgumentException("Email đã được xác thực trước đó");
        }

        user.setEmailVerified(true);
        user.setIsActive(true);
        userRepository.save(user);

        // Delete the used token
        emailVerificationTokenRepository.delete(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());

        return VerifyEmailResponse.builder()
                .message("Email của bạn đã được xác thực thành công. Bây giờ bạn có thể đăng nhập.")
                .email(user.getEmail())
                .verified(true)
                .build();
    }
    @Override
    public void sendVerificationEmail(String email) {
        log.info("Sending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEmailVerified() != null && user.getEmailVerified()) {
            throw new IllegalArgumentException("Email đã được xác thực");
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();

        // Save token to Redis with 24 hours expiration
        long ttl = 24 * 60 * 60 * 1000; // 24 hours

        try {
            // Delete old token if exists
            emailVerificationTokenRepository.deleteByEmail(email);

            emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                    .token(verificationToken)
                    .email(email)
                    .ttl(ttl)
                    .build()
            );
            log.info("Email verification token saved to Redis");
        } catch (Exception e) {
            log.error("Failed to save verification token to Redis: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo token xác thực email. Vui lòng thử lại sau.");
        }

        // Send email
        try {
            emailService.sendVerificationEmail(email, verificationToken);
            log.info("Verification email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage(), e);
            try {
                emailVerificationTokenRepository.deleteById(verificationToken);
            } catch (Exception ex) {
                log.error("Failed to cleanup token: {}", ex.getMessage());
            }
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng thử lại sau.");
        }
    }

}
