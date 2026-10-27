package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionMemoryServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    private ChatSessionMemoryService memoryService;

    @BeforeEach
    void setUp() {
        memoryService = new ChatSessionMemoryService(chatSessionRepository, new ObjectMapper());
    }

    @Test
    @DisplayName("Structured Memory: Ghi nhớ thông tin dự án, ngân sách và trích xuất ràng buộc kỹ thuật")
    void testStructuredMemoryExtractionAndPersistence() {
        ChatSession session = ChatSession.builder()
                .id(UUID.randomUUID())
                .status("active")
                .build();

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("projectType", "mini_drone");
        constraints.put("budget", new BigDecimal("3000000"));

        ChatIntentAnalyzer.AnalysisResult analysis = new ChatIntentAnalyzer.AnalysisResult(
                ChatIntentAnalyzer.Intent.BUILD_PROJECT,
                "mini_drone",
                constraints,
                List.of("kích thước khung", "môi trường hoạt động"),
                true
        );

        String userMsg = "Mình muốn build drone 3 inch bay trong nhà, dùng pin 4S, đã có tay điều khiển";
        ChatSessionMemoryService.StructuredMemory memory = memoryService.updateMemory(session, analysis, userMsg);

        assertNotNull(memory);
        assertEquals("mini_drone", memory.getProjectTopic());
        assertEquals(new BigDecimal("3000000"), memory.getBudget());
        assertEquals("3 inch", memory.getConstraints().get("frame_size"));
        assertEquals("trong nhà (indoor)", memory.getConstraints().get("environment"));
        assertEquals("4S", memory.getConstraints().get("battery_voltage"));
        assertEquals("Đã có sẵn tay điều khiển (Tx)", memory.getConstraints().get("existing_radio"));

        assertTrue(memory.getAnsweredQuestions().contains("kích thước khung"));
        assertTrue(memory.getAnsweredQuestions().contains("môi trường hoạt động"));

        verify(chatSessionRepository, atLeastOnce()).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Structured Memory: Định dạng prompt ngắn gọn, chính xác để đưa vào System Context")
    void testFormatMemoryForPrompt() {
        ChatSessionMemoryService.StructuredMemory mem = new ChatSessionMemoryService.StructuredMemory();
        mem.setProjectTopic("mini_drone");
        mem.setBudget(new BigDecimal("2500000"));
        mem.getConstraints().put("frame_size", "3 inch");
        mem.getConstraints().put("battery_voltage", "4S");

        String prompt = memoryService.formatMemoryForPrompt(mem);

        assertTrue(prompt.contains("mini_drone"));
        assertTrue(prompt.contains("2.500.000"));
        assertTrue(prompt.contains("3 inch"));
        assertTrue(prompt.contains("4S"));
        assertTrue(prompt.contains("STRUCTURED SESSION MEMORY"));
    }
}
