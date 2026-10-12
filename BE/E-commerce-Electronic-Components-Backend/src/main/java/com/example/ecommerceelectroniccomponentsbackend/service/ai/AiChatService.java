package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ai.ChatResponseDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatMessage;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatSession;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiToolService aiToolService;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${ai.ollama.model:qwen2.5:7b-instruct}")
    private String modelName;


    private static final String SYSTEM_PROMPT = """
            Bạn là Trợ lý AI chuyên gia về linh kiện điện tử của hệ thống bán hàng trực tuyến DALN.
            Nhiệm vụ của bạn:
            1. Tư vấn kỹ thuật chính xác cho khách hàng (thông số IC, điện trở, tụ điện, vi điều khiển Arduino, ESP32, Raspberry Pi, cảm biến, module nguồn).
            2. Tra cứu sản phẩm và thông số bằng cách GỌI CÔNG CỤ (tool) `search_products`.
            3. Tra cứu số lượng tồn kho và biến thể sản phẩm bằng công cụ `check_stock`.
            4. Hỗ trợ Admin kiểm tra linh kiện sắp hết hàng bằng công cụ `get_low_stock_products`.
            5. Hỗ trợ Admin xem báo cáo tổng doanh thu và số lượng đơn hàng bằng công cụ `get_revenue_report`.
            6. Tra cứu tình trạng vận chuyển và xử lý đơn hàng bằng công cụ `track_order`.
            Quy tắc trình bày:
            - Trình bày câu trả lời bằng Markdown phong phú, súc tích và mạch lạc: in đậm thông số kỹ thuật then chốt (**5V**, **16MHz**, **giá bán**), sử dụng gạch đầu dòng rõ ràng.
            - Khi so sánh hoặc liệt kê nhiều sản phẩm/thuộc tính, hãy định dạng thành bảng Markdown.
            - TUYỆT ĐỐI KHÔNG xuất văn bản thô (raw JSON) hoặc chuỗi dữ liệu lặp lại.
            - Hệ thống tự động đính kèm thẻ tương tác (Product Card, Order Tracking Card,...) từ kết quả tool bên dưới câu trả lời, do đó câu trả lời của bạn cần tập trung vào việc tóm tắt và đưa ra lời tư vấn giá trị nhất.
            """;

    @Transactional
    public ChatResponseDTO processChat(ChatRequestDTO request, Long userId, String userRole) {
        // 1. Quản lý phiên chat (Session)
        ChatSession session = resolveSession(request.getSessionId(), userId);
        UUID sessionId = session.getId();

        // 2. Lưu tin nhắn người dùng
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(request.getMessage())
                .build());

        // 3. Chuẩn bị ngữ cảnh hội thoại có thông tin vai trò
        boolean isAdmin = userRole != null && userRole.contains("ADMIN");
        java.time.LocalDate today = java.time.LocalDate.now();
        String dynamicPrompt = SYSTEM_PROMPT + "\n" +
                "THÔNG TIN PHIÊN VÀ PHÂN QUYỀN HIỆN TẠI:\n" +
                "- Ngày hệ thống hiện tại: " + today + " (Năm " + today.getYear() + ", Tháng " + today.getMonthValue() + ")\n" +
                "- Vai trò người dùng: " + (isAdmin ? "ADMIN (QUẢN TRỊ VIÊN HỆ THỐNG)" : "KHÁCH HÀNG / GUEST") + "\n" +
                "- HƯỚNG DẪN BẮT BUỘC:\n" +
                (isAdmin
                    ? "  + Người dùng này LÀ QUẢN TRỊ VIÊN (ADMIN). Họ có toàn quyền tra cứu tồn kho, cảnh báo hàng sắp hết (gọi tool get_low_stock_products), báo cáo doanh thu theo ngày/tháng (gọi tool get_revenue_report), và báo cáo hàng bán chạy nhất (gọi tool get_top_selling_products). BẮT BUỘC PHẢI GỌI TOOL ngay, TUYỆT ĐỐI KHÔNG TỪ CHỐI hay bảo họ liên hệ Admin vì chính họ là Admin!\n"
                    : "  + Khi khách hàng hỏi về mặt hàng bán chạy, hãy gọi tool get_top_selling_products để giới thiệu cho họ.\n"
                ) +
                "  + Khi người dùng hỏi về tình trạng đơn hàng hoặc cung cấp mã đơn (chứa tiền tố ORD hoặc số đơn), hãy gọi tool track_order để kiểm tra tình trạng thực tế.\n";

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", dynamicPrompt));


        // Lấy các tin nhắn trước đó (tối đa 10 tin gần nhất)
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        int startIndex = Math.max(0, history.size() - 10);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // 4. Lấy danh sách tool definitions
        List<Map<String, Object>> tools = aiToolService.getToolDefinitions();
        List<String> toolsUsed = new ArrayList<>();
        List<Object> referencedData = new ArrayList<>();

        // 5. Gọi Ollama
        OllamaClient.OllamaChatResult result = ollamaClient.chat(messages, tools);

        String finalContent;

        if (result.success()) {
            if (result.toolCalls() != null && !result.toolCalls().isEmpty()) {
                // LLM muốn gọi Tool
                messages.add(Map.of(
                        "role", "assistant",
                        "content", result.content() != null ? result.content() : "",
                        "tool_calls", result.toolCalls()
                ));

                for (Map<String, Object> toolCall : result.toolCalls()) {
                    Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                    String functionName = (String) function.get("name");
                    Map<String, Object> args = parseArguments(function.get("arguments"));

                    toolsUsed.add(functionName);
                    Map<String, Object> toolOutput = executeTool(functionName, args, sessionId, userId, userRole);
                    referencedData.add(toolOutput);

                    try {
                        messages.add(Map.of(
                                "role", "tool",
                                "name", functionName,
                                "content", objectMapper.writeValueAsString(toolOutput)
                        ));
                    } catch (Exception e) {
                        messages.add(Map.of(
                                "role", "tool",
                                "name", functionName,
                                "content", toolOutput.toString()
                        ));
                    }
                }

                // Gọi LLM lần 2 để tổng hợp câu trả lời sau khi đã có kết quả tool
                OllamaClient.OllamaChatResult finalTurn = ollamaClient.chat(messages, null);
                finalContent = finalTurn.success() && finalTurn.content() != null
                        ? finalTurn.content()
                        : "Đã hoàn thành tra cứu dữ liệu từ hệ thống.";
            } else {
                finalContent = result.content();
            }
        } else {
            // Fallback khi Ollama chưa bật: Tự động tra cứu trực tiếp từ khóa
            log.info("Ollama không khả dụng, kích hoạt fallback cơ bản");
            finalContent = handleFallback(request.getMessage(), sessionId, userId, userRole, toolsUsed, referencedData);
        }

        // 6. Lưu câu trả lời của trợ lý
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(finalContent)
                .modelName(this.modelName)
                .build());


        return ChatResponseDTO.builder()
                .sessionId(sessionId)
                .message(finalContent)
                .toolsUsed(toolsUsed)
                .referencedData(referencedData)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private ChatSession resolveSession(UUID sessionId, Long userId) {
        if (sessionId != null) {
            Optional<ChatSession> opt = chatSessionRepository.findById(sessionId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return chatSessionRepository.save(ChatSession.builder()
                .userId(userId)
                .channel("web")
                .status("active")
                .build());
    }

    private Map<String, Object> executeTool(String toolName, Map<String, Object> args, UUID sessionId, Long userId, String role) {
        return switch (toolName) {
            case "search_products" -> {
                String query = (String) args.getOrDefault("query", "");
                yield aiToolService.searchProducts(query, sessionId, userId);
            }
            case "check_stock" -> {
                Number prodId = (Number) args.get("productId");
                yield aiToolService.checkStock(prodId != null ? prodId.longValue() : 1L, sessionId, userId);
            }
            case "get_low_stock_products" -> {
                Number threshold = (Number) args.getOrDefault("threshold", 10);
                yield aiToolService.getLowStockProducts(threshold != null ? threshold.intValue() : 10, role, sessionId, userId);
            }
            case "get_top_selling_products" -> {
                Number limit = (Number) args.getOrDefault("limit", 5);
                yield aiToolService.getTopSellingProducts(limit != null ? limit.intValue() : 5, sessionId, userId);
            }
            case "get_revenue_report" -> {
                String startDate = (String) args.get("start_date");
                String endDate = (String) args.get("end_date");
                String period = (String) args.getOrDefault("period", "range");
                yield aiToolService.getRevenueReport(startDate, endDate, period, role, sessionId, userId);
            }
            case "track_order" -> {
                String orderCode = (String) args.getOrDefault("order_code", "");
                yield aiToolService.trackOrder(orderCode, sessionId, userId);
            }
            default -> Map.of("error", "Công cụ không được hỗ trợ: " + toolName);
        };
    }

    private Map<String, Object> parseArguments(Object rawArgs) {
        if (rawArgs instanceof Map) {
            return (Map<String, Object>) rawArgs;
        } else if (rawArgs instanceof String str) {
            try {
                return objectMapper.readValue(str, Map.class);
            } catch (Exception e) {
                return Map.of();
            }
        }
        return Map.of();
    }

    private String handleFallback(String message, UUID sessionId, Long userId, String userRole, List<String> toolsUsed, List<Object> referencedData) {
        String lower = message.toLowerCase();
        if (lower.contains("tồn kho") || lower.contains("cảnh báo") || lower.contains("hết hàng")) {
            toolsUsed.add("get_low_stock_products");
            Map<String, Object> res = aiToolService.getLowStockProducts(10, userRole, sessionId, userId);
            referencedData.add(res);
            if (res.containsKey("error")) {
                return (String) res.get("error");
            }
            return "*(Ollama local chưa bật - Phản hồi tự động)*\nĐã tra cứu các mặt hàng sắp hết hàng (tồn kho <= 10). Bạn có thể xem chi tiết trong kết quả đi kèm.";
        } else if (lower.contains("bán chạy") || lower.contains("top bán") || lower.contains("đắt hàng") || lower.contains("mua nhiều")) {
            toolsUsed.add("get_top_selling_products");
            Map<String, Object> res = aiToolService.getTopSellingProducts(5, sessionId, userId);
            referencedData.add(res);
            return "*(Ollama local chưa bật - Phản hồi tự động)*\nĐã tra cứu top các mặt hàng bán chạy nhất của cửa hàng.";
        } else if (lower.contains("doanh thu") || lower.contains("báo cáo doanh thu") || lower.contains("tiền bán")) {
            toolsUsed.add("get_revenue_report");
            java.time.LocalDate now = java.time.LocalDate.now();
            String start = now.withDayOfMonth(1).toString();
            String end = now.toString();
            Map<String, Object> res = aiToolService.getRevenueReport(start, end, "month", userRole, sessionId, userId);
            referencedData.add(res);
            if (res.containsKey("error")) {
                return (String) res.get("error");
            }
            return "*(Ollama local chưa bật - Phản hồi tự động)*\n" + res.getOrDefault("summary", "Đã thống kê doanh thu tháng này.");
        } else if (lower.contains("đơn hàng") || lower.contains("tra cứu đơn") || lower.contains("mã đơn") || lower.contains("ord-") || lower.contains("#ord")) {
            toolsUsed.add("track_order");
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)(?:#|ord-)[a-z0-9-]+").matcher(message);
            String code = m.find() ? m.group() : message.trim();
            Map<String, Object> res = aiToolService.trackOrder(code, sessionId, userId);
            referencedData.add(res);
            if (res.containsKey("error")) {
                return "*(Ollama local chưa bật - Phản hồi tự động)*\n" + res.get("error");
            }
            return "*(Ollama local chưa bật - Phản hồi tự động)*\n" + res.getOrDefault("message", "Đã tra cứu thông tin đơn hàng.");
        } else {
            toolsUsed.add("search_products");

            Map<String, Object> res = aiToolService.searchProducts(message, sessionId, userId);
            referencedData.add(res);
            int count = (int) res.getOrDefault("totalFound", 0);
            if (count > 0) {
                return "*(Ollama local chưa bật - Phản hồi tự động)*\nTôi đã tìm thấy " + count + " sản phẩm phù hợp với yêu cầu của bạn trong cơ sở dữ liệu.";
            } else {
                return "*(Ollama local chưa bật - Phản hồi tự động)*\nHiện tại chưa tìm thấy linh kiện nào khớp chính xác với từ khóa \"" + message + "\". Bạn vui lòng kiểm tra lại tên linh kiện hoặc bật Ollama (`ollama run qwen2.5:7b`) để nhận tư vấn chi tiết.";
            }
        }
    }
}
