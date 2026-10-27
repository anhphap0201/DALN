package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quản lý bộ nhớ ngữ cảnh có cấu trúc (Structured Session Memory):
 * Duy trì thông tin dự án, ngân sách, ràng buộc kỹ thuật đã xác định xuyên suốt các lượt chat.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionMemoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final ObjectMapper objectMapper;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StructuredMemory {
        private String projectTopic;
        private BigDecimal budget;
        @Builder.Default
        private Map<String, String> constraints = new LinkedHashMap<>();
        @Builder.Default
        private List<String> selectedComponents = new ArrayList<>();
        @Builder.Default
        private List<String> answeredQuestions = new ArrayList<>();
        private String lastIntent;
    }

    /**
     * Lấy bộ nhớ có cấu trúc từ session hiện tại
     */
    public StructuredMemory getMemory(ChatSession session) {
        if (session == null || session.getContextData() == null || session.getContextData().isBlank()) {
            return new StructuredMemory();
        }
        try {
            return objectMapper.readValue(session.getContextData(), StructuredMemory.class);
        } catch (Exception e) {
            log.warn("Không thể parse StructuredMemory từ session {}: {}", session.getId(), e.getMessage());
            return new StructuredMemory();
        }
    }

    /**
     * Cập nhật bộ nhớ sau mỗi tin nhắn người dùng
     */
    public StructuredMemory updateMemory(ChatSession session, ChatIntentAnalyzer.AnalysisResult analysis, String userMessage) {
        StructuredMemory memory = getMemory(session);
        boolean modified = false;

        // 1. Cập nhật dự án nếu có
        if (analysis.projectTopic() != null && !analysis.projectTopic().isBlank()) {
            if (!Objects.equals(memory.getProjectTopic(), analysis.projectTopic())) {
                memory.setProjectTopic(analysis.projectTopic());
                modified = true;
            }
        }

        // 2. Cập nhật ngân sách nếu người dùng cung cấp
        if (analysis.constraints() != null && analysis.constraints().containsKey("budget")) {
            Object bObj = analysis.constraints().get("budget");
            if (bObj instanceof BigDecimal bd) {
                memory.setBudget(bd);
                modified = true;
            } else if (bObj != null) {
                memory.setBudget(new BigDecimal(bObj.toString()));
                modified = true;
            }
        }

        // 3. Trích xuất thêm các ràng buộc kỹ thuật từ tin nhắn
        String lower = normalize(userMessage);

        // Kích thước khung (frame size)
        Pattern framePat = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(inch|in|\")");
        Matcher frameMat = framePat.matcher(lower);
        if (frameMat.find()) {
            memory.getConstraints().put("frame_size", frameMat.group(1) + " inch");
            memory.getAnsweredQuestions().add("kích thước khung");
            modified = true;
        }

        // Môi trường hoạt động
        if (lower.contains("trong nha") || lower.contains("indoor")) {
            memory.getConstraints().put("environment", "trong nhà (indoor)");
            memory.getAnsweredQuestions().add("môi trường hoạt động");
            modified = true;
        } else if (lower.contains("ngoai troi") || lower.contains("outdoor")) {
            memory.getConstraints().put("environment", "ngoài trời (outdoor)");
            memory.getAnsweredQuestions().add("môi trường hoạt động");
            modified = true;
        }

        // Loại pin / Điện áp (3S, 4S, 6S, 12V, 5V...)
        Pattern lipoPat = Pattern.compile("\\b([1-6]s)\\b");
        Matcher lipoMat = lipoPat.matcher(lower);
        if (lipoMat.find()) {
            memory.getConstraints().put("battery_voltage", lipoMat.group(1).toUpperCase());
            modified = true;
        }

        // Nhu cầu Camera / FPV
        if (lower.contains("co camera") || lower.contains("co fpv") || lower.contains("can camera")) {
            memory.getConstraints().put("camera", "Có camera FPV");
            memory.getAnsweredQuestions().add("camera");
            modified = true;
        } else if (lower.contains("khong camera") || lower.contains("ko camera") || lower.contains("khong can camera")) {
            memory.getConstraints().put("camera", "Không cần camera");
            memory.getAnsweredQuestions().add("camera");
            modified = true;
        }

        // Đã có sẵn thiết bị (Radio transmitter, sạc, pin...)
        if (lower.contains("da co tay dieu khien") || lower.contains("da co tx") || lower.contains("co tay cam")) {
            memory.getConstraints().put("existing_radio", "Đã có sẵn tay điều khiển (Tx)");
            memory.getAnsweredQuestions().add("thiết bị đã có sẵn");
            modified = true;
        }
        if (lower.contains("da co pin") || lower.contains("da co sac")) {
            memory.getConstraints().put("existing_power", "Đã có sẵn pin/sạc");
            modified = true;
        }

        // 4. Lưu lại intent
        if (analysis.intent() != null) {
            memory.setLastIntent(analysis.intent().name());
            modified = true;
        }

        // Lưu vào DB nếu có thay đổi
        if (modified) {
            try {
                session.setContextData(objectMapper.writeValueAsString(memory));
                chatSessionRepository.save(session);
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi lưu StructuredMemory: {}", e.getMessage());
            }
        }

        return memory;
    }

    /**
     * Tạo chuỗi ngữ cảnh tiêm vào System Prompt cho Ollama
     */
    public String formatMemoryForPrompt(StructuredMemory memory) {
        if (memory == null || (memory.getProjectTopic() == null && memory.getBudget() == null && memory.getConstraints().isEmpty())) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n[BỘ NHỚ NGỮ CẢNH ĐÃ XÁC ĐỊNH TRONG PHIÊN (STRUCTURED SESSION MEMORY)]:\n");
        if (memory.getProjectTopic() != null) {
            sb.append("- Dự án người dùng đang chế tạo: ").append(memory.getProjectTopic()).append("\n");
        }
        if (memory.getBudget() != null) {
            sb.append(String.format(Locale.GERMANY, "- Ngân sách dự kiến: %,d đ\n", memory.getBudget().longValue()));
        }
        if (!memory.getConstraints().isEmpty()) {
            sb.append("- Các ràng buộc kỹ thuật đã thống nhất:\n");
            memory.getConstraints().forEach((k, v) -> sb.append("  + ").append(k).append(": ").append(v).append("\n"));
        }
        sb.append("(QUY TẮC: Ghi nhớ các thông tin trên xuyên suốt cuộc trò chuyện; không hỏi lại những điều người dùng đã trả lời!)\n");
        return sb.toString();
    }

    private String normalize(String text) {
        if (text == null) return "";
        String n = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        String withoutMarks = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutMarks.replace('đ', 'd').replace('Đ', 'd');
    }
}
