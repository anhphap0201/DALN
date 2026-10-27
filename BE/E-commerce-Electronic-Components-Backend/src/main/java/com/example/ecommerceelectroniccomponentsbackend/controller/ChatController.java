package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatResponseDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatMessage;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.ai.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chatbot", description = "APIs for AI-powered assistant and inventory Q&A")
public class ChatController {

    private final AiChatService aiChatService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatToolLogRepository chatToolLogRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCurrentUserRole() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getRole() != null) {
                return userOpt.get().getRole().name();
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "ROLE_GUEST";
        }
        boolean hasAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.contains("ADMIN"));
        return hasAdmin ? "ROLE_ADMIN" : "ROLE_USER";
    }


    @PostMapping
    @Operation(summary = "Send message to AI Chatbot (Supports tool calling for stock & products)")
    public ResponseEntity<ChatResponseDTO> sendMessage(@Valid @RequestBody ChatRequestDTO request) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        ChatResponseDTO response = aiChatService.processChat(request, userId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get list of chat sessions for current user")
    public ResponseEntity<List<ChatSession>> getMySessions() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get message history for a specific chat session")
    public ResponseEntity<List<ChatMessage>> getSessionMessages(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "Get AI operational stats & metrics (Admin only)")
    public ResponseEntity<?> getAiOperationalStats() {
        String role = getCurrentUserRole();
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Access denied. Admin role required."));
        }
        long totalSessions = chatSessionRepository.count();
        long totalMessages = chatMessageRepository.count();
        long totalToolCalls = chatToolLogRepository.count();
        long successfulToolCalls = chatToolLogRepository.countByStatus("SUCCESS");
        long failedToolCalls = chatToolLogRepository.countByStatus("FAILED");
        Double avgExecutionTime = chatToolLogRepository.getAverageExecutionTime();
        double successRate = totalToolCalls > 0 ? (double) successfulToolCalls / totalToolCalls * 100.0 : 100.0;

        List<Object[]> topToolsRaw = chatToolLogRepository.getTopToolsUsed();
        List<java.util.Map<String, Object>> topTools = topToolsRaw != null ? topToolsRaw.stream()
                .limit(5)
                .map(row -> java.util.Map.of(
                        "toolName", row[0] != null ? row[0] : "unknown",
                        "callCount", row[1] != null ? row[1] : 0
                ))
                .toList() : List.of();

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalSessions", totalSessions);
        stats.put("totalMessages", totalMessages);
        stats.put("totalToolCalls", totalToolCalls);
        stats.put("successfulToolCalls", successfulToolCalls);
        stats.put("failedToolCalls", failedToolCalls);
        stats.put("toolSuccessRatePercent", Math.round(successRate * 100.0) / 100.0);
        stats.put("avgExecutionTimeMs", avgExecutionTime != null ? Math.round(avgExecutionTime * 100.0) / 100.0 : 0.0);
        stats.put("topToolsUsed", topTools);

        return ResponseEntity.ok(stats);
    }
}
