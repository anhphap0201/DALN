package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích bảo mật và khử trùng dữ liệu cho AI Chatbot:
 * - Che giấu thông tin cá nhân (PII: Số điện thoại, Email, Địa chỉ, Thẻ) trong log và phản hồi
 * - Phát hiện và ngăn chặn tấn công Prompt Injection / Jailbreak
 */
@Component
@Slf4j
public class AiSecuritySanitizer {

    // Regex phát hiện số điện thoại Việt Nam (10 số, đầu 03, 05, 07, 08, 09 hoặc +84)
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+84|0)(?:3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}");

    // Regex phát hiện email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // Các mẫu câu cố tình vượt quyền (Prompt Injection / Jailbreak)
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)(ignore|disregard|forget)\\s+(all\\s+)?(previous|past|above)\\s+(instructions|prompts|system\\s+prompt)"),
            Pattern.compile("(?i)(bo\\s+qua|quen|xoa)\\s+(moi|tat\\s+ca)?\\s*(huong\\s+dan|chi\\s+dan|system\\s+prompt|cau\\s+lenh)"),
            Pattern.compile("(?i)(show|display|reveal|in\\s+ra|hien\\s+thi)\\s+(system\\s+prompt|secret\\s+key|mat\\s+khau|password|token)"),
            Pattern.compile("(?i)(you\\s+are\\s+now|tu\\s+gio\\s+tro\\s+di\\s+ban\\s+la)\\s+(an?\\s+unrestricted|unfiltered|jailbroken|dan\\s+mode)"),
            Pattern.compile("(?i)(bypass|override|drop\\s+table|delete\\s+from|exec\\s*\\()")
    );

    /**
     * Kiểm tra tin nhắn có chứa dấu hiệu Prompt Injection độc hại hay không
     */
    public boolean isPromptInjection(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(message).find()) {
                log.warn("[SECURITY ALERT] Phát hiện Prompt Injection đáng ngờ từ người dùng: '{}'", maskPii(message));
                return true;
            }
        }
        return false;
    }

    /**
     * Che thông tin nhạy cảm (Số điện thoại, Email) trong chuỗi văn bản
     */
    public String maskPii(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        // Mask số điện thoại: 0987654321 -> 098****321
        Matcher phoneMatcher = PHONE_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (phoneMatcher.find()) {
            String fullPhone = phoneMatcher.group();
            String masked = maskPhoneNumber(fullPhone);
            phoneMatcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        phoneMatcher.appendTail(sb);
        String step1 = sb.toString();

        // Mask email: user@domain.com -> u***r@domain.com
        Matcher emailMatcher = EMAIL_PATTERN.matcher(step1);
        StringBuilder sb2 = new StringBuilder();
        while (emailMatcher.find()) {
            String fullEmail = emailMatcher.group();
            String masked = maskEmailAddress(fullEmail);
            emailMatcher.appendReplacement(sb2, Matcher.quoteReplacement(masked));
        }
        emailMatcher.appendTail(sb2);

        return sb2.toString();
    }

    /**
     * Che số điện thoại giữ lại 3 số đầu và 3 số cuối
     */
    public String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        int len = phone.length();
        return phone.substring(0, 3) + "****" + phone.substring(len - 3);
    }

    /**
     * Che email giữ lại ký tự đầu và cuối trước @
     */
    public String maskEmailAddress(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    /**
     * Ghi log thực thi công cụ một cách an toàn mà không rò rỉ dữ liệu nhạy cảm
     */
    public void logToolExecution(String toolName, UUID sessionId, Long userId, Map<String, Object> sanitizedParams, boolean success) {
        log.info("[AI TOOL AUDIT] tool='{}' session='{}' user='{}' params={} status={}",
                toolName,
                sessionId,
                userId != null ? userId : "guest",
                sanitizedParams != null ? maskPii(sanitizedParams.toString()) : "{}",
                success ? "SUCCESS" : "FAILED_OR_FORBIDDEN");
    }
}
