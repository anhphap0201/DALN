package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChatIntentAnalyzerTest {

    private final ChatIntentAnalyzer analyzer = new ChatIntentAnalyzer();

    @Test
    @DisplayName("Phát hiện intent BUILD_PROJECT và thiếu thông tin cho mini drone")
    void testBuildMiniDroneIntentMissingConstraints() {
        String query = "Tôi cần build một mini drone, hãy liệt kê linh kiện cần thiết.";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.BUILD_PROJECT, result.intent());
        assertEquals("mini_drone", result.projectTopic());
        assertTrue(result.needsClarification());
        assertFalse(result.missingInformation().isEmpty());
        // Kiểm tra có hỏi về kích thước khung, môi trường bay, ngân sách
        assertTrue(result.missingInformation().stream().anyMatch(q -> q.contains("kích thước khung") || q.contains("khung")));
        assertTrue(result.missingInformation().stream().anyMatch(q -> q.contains("ngân sách") || q.contains("Ngân sách")));
    }

    @Test
    @DisplayName("Phát hiện intent BUILD_PROJECT với ngân sách và cỡ khung đã có")
    void testBuildMiniDroneWithSomeConstraints() {
        String query = "Tôi muốn build drone 3 inch bay ngoài trời ngân sách 3 triệu.";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.BUILD_PROJECT, result.intent());
        assertEquals("mini_drone", result.projectTopic());
        assertEquals("3 inch", result.constraints().get("frame_size"));
        assertEquals("outdoor", result.constraints().get("environment"));
        assertEquals(new BigDecimal("3000000"), result.constraints().get("budget"));
    }

    @Test
    @DisplayName("Phát hiện intent ORDER_TRACKING kèm mã đơn và số điện thoại")
    void testOrderTrackingIntent() {
        String query = "Tra cứu đơn hàng #ORD-98765 số điện thoại 0912345678 giùm tôi";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.ORDER_TRACKING, result.intent());
        assertEquals("#ORD-98765", result.constraints().get("orderCode"));
        assertEquals("0912345678", result.constraints().get("phoneNumber"));
    }

    @Test
    @DisplayName("Phát hiện intent PRODUCT_COMPARE")
    void testProductCompareIntent() {
        String query = "So sánh Arduino Uno R3 với Raspberry Pi 5";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.PRODUCT_COMPARE, result.intent());
    }

    @Test
    @DisplayName("Phát hiện intent PRODUCT_RECOMMEND")
    void testProductRecommendIntent() {
        String query = "Gợi ý cho mình linh kiện Arduino dưới 200k";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.PRODUCT_RECOMMEND, result.intent());
    }

    @Test
    @DisplayName("Phát hiện intent CHIT_CHAT")
    void testChitChatIntent() {
        String query = "Xin chào bạn, hôm nay thế nào?";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.CHIT_CHAT, result.intent());
    }

    @Test
    @DisplayName("Phát hiện intent PRODUCT_SEARCH cho câu hỏi tra cứu linh kiện đơn lẻ")
    void testProductSearchIntentForSingleItem() {
        String query1 = "cửa hàng có motor không";
        assertEquals(ChatIntentAnalyzer.Intent.PRODUCT_SEARCH, analyzer.analyze(query1).intent());

        String query2 = "shop có bán pin lipo không?";
        assertEquals(ChatIntentAnalyzer.Intent.PRODUCT_SEARCH, analyzer.analyze(query2).intent());

        String query3 = "còn cánh quạt không";
        assertEquals(ChatIntentAnalyzer.Intent.PRODUCT_SEARCH, analyzer.analyze(query3).intent());
    }

    @Test
    @DisplayName("Phát hiện intent WARRANTY_INQUIRY kèm mã Serial")
    void testWarrantyInquiryIntent() {
        String query = "Kiểm tra bảo hành cho tem SN-202610-A1B2C3D4";
        ChatIntentAnalyzer.AnalysisResult result = analyzer.analyze(query);

        assertEquals(ChatIntentAnalyzer.Intent.WARRANTY_INQUIRY, result.intent());
        assertEquals("SN-202610-A1B2C3D4", result.constraints().get("serialNumber"));
    }
}
