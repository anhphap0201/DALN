package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatResponseDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatMessage;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.model.Role;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.ai.AiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private AiChatService aiChatService;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatToolLogRepository chatToolLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatController chatController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Admin gửi tin nhắn -> ChatController nhận diện đúng ROLE_ADMIN và userId")
    void testSendMessage_AsAdmin_Success() throws Exception {
        Long adminUserId = 100L;
        User adminUser = User.builder().id(adminUserId).role(Role.ROLE_ADMIN).email("admin@shop.com").build();
        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));

        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(adminUserId), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID sessionId = UUID.randomUUID();
        ChatResponseDTO mockResponse = ChatResponseDTO.builder()
                .sessionId(sessionId)
                .message("Báo cáo doanh thu tháng này đạt 150.000.000 VNĐ.")
                .toolsUsed(List.of("get_revenue_report"))
                .build();

        when(aiChatService.processChat(any(ChatRequestDTO.class), eq(adminUserId), eq("ROLE_ADMIN")))
                .thenReturn(mockResponse);

        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Báo cáo doanh thu tháng này")
                .build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is(sessionId.toString())))
                .andExpect(jsonPath("$.message", containsString("150.000.000")))
                .andExpect(jsonPath("$.toolsUsed", hasItem("get_revenue_report")));

        verify(aiChatService, times(1)).processChat(any(ChatRequestDTO.class), eq(adminUserId), eq("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("User thường gửi tin nhắn -> ChatController nhận diện đúng ROLE_USER và userId")
    void testSendMessage_AsUser_Success() throws Exception {
        Long regularUserId = 200L;
        User regularUser = User.builder().id(regularUserId).role(Role.ROLE_USER).email("user@gmail.com").build();
        when(userRepository.findById(regularUserId)).thenReturn(Optional.of(regularUser));

        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(regularUserId), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID sessionId = UUID.randomUUID();
        ChatResponseDTO mockResponse = ChatResponseDTO.builder()
                .sessionId(sessionId)
                .message("Arduino Uno R3 có sẵn hàng với giá 150.000 VNĐ.")
                .toolsUsed(List.of("search_products"))
                .build();

        when(aiChatService.processChat(any(ChatRequestDTO.class), eq(regularUserId), eq("ROLE_USER")))
                .thenReturn(mockResponse);

        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Tư vấn Arduino Uno R3")
                .build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is(sessionId.toString())))
                .andExpect(jsonPath("$.message", containsString("Arduino Uno R3")));

        verify(aiChatService, times(1)).processChat(any(ChatRequestDTO.class), eq(regularUserId), eq("ROLE_USER"));
    }

    @Test
    @DisplayName("Khách vãng lai gửi tin nhắn -> ChatController gán userId=null và role=ROLE_GUEST")
    void testSendMessage_AsGuest_Success() throws Exception {
        SecurityContextHolder.clearContext();

        UUID sessionId = UUID.randomUUID();
        ChatResponseDTO mockResponse = ChatResponseDTO.builder()
                .sessionId(sessionId)
                .message("Chào mừng quý khách đến với ECShop!")
                .build();

        when(aiChatService.processChat(any(ChatRequestDTO.class), isNull(), eq("ROLE_GUEST")))
                .thenReturn(mockResponse);

        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Cửa hàng bán những linh kiện gì?")
                .build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Chào mừng")));

        verify(aiChatService, times(1)).processChat(any(ChatRequestDTO.class), isNull(), eq("ROLE_GUEST"));
    }

    @Test
    @DisplayName("Tin nhắn rỗng -> Trả về lỗi 400 Bad Request từ Jakarta Validation")
    void testSendMessage_BlankMessage_ReturnsBadRequest() throws Exception {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("   ")
                .build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aiChatService, never()).processChat(any(), any(), any());
    }

    @Test
    @DisplayName("Lấy danh sách phiên chat của user đăng nhập thành công")
    void testGetMySessions_AuthenticatedUser() throws Exception {
        Long userId = 50L;
        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChatSession session1 = ChatSession.builder().id(UUID.randomUUID()).userId(userId).build();
        ChatSession session2 = ChatSession.builder().id(UUID.randomUUID()).userId(userId).build();
        when(chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Khách vãng lai gọi lấy danh sách phiên chat -> Trả về mảng rỗng")
    void testGetMySessions_GuestUser() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(chatSessionRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("Lấy lịch sử tin nhắn của một phiên chat")
    void testGetSessionMessages() throws Exception {
        UUID sessionId = UUID.randomUUID();
        ChatMessage msg1 = ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content("Drone 3 inch")
                .build();
        ChatMessage msg2 = ChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content("Bảng BOM đã được lập.")
                .build();

        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
                .thenReturn(List.of(msg1, msg2));

        mockMvc.perform(get("/api/chat/sessions/{sessionId}/messages", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("Drone 3 inch")))
                .andExpect(jsonPath("$[1].content", is("Bảng BOM đã được lập.")));
    }

    @Test
    @DisplayName("Admin gọi lấy thống kê vận hành AI -> 200 OK kèm các chỉ số metrics")
    void testGetAiOperationalStats_Admin_Success() throws Exception {
        Long adminUserId = 100L;
        User adminUser = User.builder().id(adminUserId).role(Role.ROLE_ADMIN).email("admin@shop.com").build();
        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));

        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(adminUserId), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(chatSessionRepository.count()).thenReturn(10L);
        when(chatMessageRepository.count()).thenReturn(50L);
        when(chatToolLogRepository.count()).thenReturn(20L);
        when(chatToolLogRepository.countByStatus("SUCCESS")).thenReturn(18L);
        when(chatToolLogRepository.countByStatus("FAILED")).thenReturn(2L);
        when(chatToolLogRepository.getAverageExecutionTime()).thenReturn(350.5);

        List<Object[]> mockTopTools = List.of(
                new Object[]{"check_stock", 12L},
                new Object[]{"search_products", 8L}
        );
        when(chatToolLogRepository.getTopToolsUsed()).thenReturn(mockTopTools);

        mockMvc.perform(get("/api/chat/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions", is(10)))
                .andExpect(jsonPath("$.totalMessages", is(50)))
                .andExpect(jsonPath("$.totalToolCalls", is(20)))
                .andExpect(jsonPath("$.successfulToolCalls", is(18)))
                .andExpect(jsonPath("$.failedToolCalls", is(2)))
                .andExpect(jsonPath("$.toolSuccessRatePercent", is(90.0)))
                .andExpect(jsonPath("$.avgExecutionTimeMs", is(350.5)))
                .andExpect(jsonPath("$.topToolsUsed", hasSize(2)))
                .andExpect(jsonPath("$.topToolsUsed[0].toolName", is("check_stock")))
                .andExpect(jsonPath("$.topToolsUsed[0].callCount", is(12)));
    }

    @Test
    @DisplayName("User thường hoặc Guest gọi thống kê vận hành AI -> 403 Forbidden")
    void testGetAiOperationalStats_UserOrGuest_Forbidden() throws Exception {
        Long normalUserId = 50L;
        User normalUser = User.builder().id(normalUserId).role(Role.ROLE_USER).email("user@shop.com").build();
        when(userRepository.findById(normalUserId)).thenReturn(Optional.of(normalUser));

        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(normalUserId), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/chat/admin/stats"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Access denied")));

        verify(chatToolLogRepository, never()).getAverageExecutionTime();
    }
}
