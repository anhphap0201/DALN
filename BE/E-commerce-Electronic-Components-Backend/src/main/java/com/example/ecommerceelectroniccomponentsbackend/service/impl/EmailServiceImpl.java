package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.exception.EmailNotVerifiedException;
import com.example.ecommerceelectroniccomponentsbackend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            logger.info("Preparing to send verification email to: {}", toEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("E-commerce Electronic Components - Email Verification");
            String emailBody = verifyEmail(token);
            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Verification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            throw new EmailNotVerifiedException("Failed to send verification email.");
        }
    }

    private String verifyEmail(String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        return "Chào mừng đến với E-commerce Electronic Components! \n\n"
                + "Cảm ơn bạn đã đăng ký. Vui lòng xác thực địa chỉ email của bạn bằng cách nhấp vào liên kết bên dưới: \n\n"
                + verificationLink
                + "\n\n"
                + "Liên kết này sẽ hết hạn sau 24 giờ.\n\n"
                + "Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\n"
                + "E-commerce Electronic Components Team";
    }

    private String resetPassword(String token) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        return "Xin chào, \n\n"
                + "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu của bạn. Nhấp vào liên kết bên dưới để đặt lại mật khẩu: \n\n"
                + resetLink
                + "\n\n"
                + "Liên kết này sẽ hết hạn sau 1 giờ.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\n"
                + "E-commerce Electronic Components Team";
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            logger.info("Preparing to send password reset email to: {}", toEmail);
            logger.debug("Frontend URL: {}, From email: {}", frontendUrl, fromEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("E-commerce Electronic Components - Đặt lại mật khẩu");
            String emailBody = resetPassword(token);
            message.setText(emailBody);

            logger.info("Sending email via SMTP...");
            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
}
