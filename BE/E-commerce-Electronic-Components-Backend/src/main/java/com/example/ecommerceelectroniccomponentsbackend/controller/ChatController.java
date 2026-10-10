package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatResponseDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatMessage;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
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
}
