package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Bước 6: Dịch vụ kiểm soát quyền và chính sách bảo mật cho AI Tools (Policy Service).
 * Đảm bảo:
 * - Kiểm tra quyền cứng tại Service layer, không dựa vào phán đoán của LLM
 * - Phân định rõ ràng ma trận phân quyền: PUBLIC, ADMIN_ONLY, OWNER_OR_VERIFIED
 * - Bảo vệ tra cứu đơn hàng: Admin xem toàn bộ, User xem đơn của mình, Guest cần khớp SĐT
 * - Kiểm soát rò rỉ dữ liệu PII và audit log an toàn
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiToolPolicyService {

    public enum ToolAccessLevel {
        PUBLIC,
        ADMIN_ONLY,
        OWNER_OR_VERIFIED
    }

    private static final Set<String> ADMIN_TOOLS = Set.of(
            "get_low_stock_products",
            "get_revenue_report",
            "get_order_analytics",
            "get_inventory_report",
            "get_product_performance",
            "get_customer_analytics",
            "get_customer_inquiry_analytics"
    );

    private static final Set<String> PUBLIC_TOOLS = Set.of(
            "search_products",
            "recommend_products",
            "compare_products",
            "check_stock",
            "get_top_selling_products",
            "find_project_components",
            "check_component_compatibility",
            "build_bill_of_materials",
            "check_warranty"
    );

    private static final String TRACK_ORDER_TOOL = "track_order";

    @Getter
    public static class ToolAccessDecision {
        private final boolean allowed;
        private final String reason;

        private ToolAccessDecision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public static ToolAccessDecision allow() {
            return new ToolAccessDecision(true, "Truy cập hợp lệ.");
        }

        public static ToolAccessDecision deny(String reason) {
            return new ToolAccessDecision(false, reason);
        }
    }

    @Getter
    public static class OrderAccessDecision {
        private final boolean allowed;
        private final boolean fullDetails;
        private final String accessType;
        private final String reason;

        private OrderAccessDecision(boolean allowed, boolean fullDetails, String accessType, String reason) {
            this.allowed = allowed;
            this.fullDetails = fullDetails;
            this.accessType = accessType;
            this.reason = reason;
        }

        public static OrderAccessDecision allow(boolean fullDetails, String accessType) {
            return new OrderAccessDecision(true, fullDetails, accessType, "Xác thực quyền xem đơn hàng thành công.");
        }

        public static OrderAccessDecision deny(String reason) {
            return new OrderAccessDecision(false, false, "DENIED", reason);
        }
    }

    /**
     * Kiểm tra người dùng có quyền thực thi công cụ (Tool) cụ thể hay không
     */
    public ToolAccessDecision checkToolAccess(String toolName, String userRole, Long userId) {
        if (toolName == null || toolName.isBlank()) {
            return ToolAccessDecision.deny("Tên công cụ không được để trống.");
        }

        if (ADMIN_TOOLS.contains(toolName)) {
            if (!isAdmin(userRole)) {
                log.warn("[RBAC REJECT] Từ chối truy cập công cụ quản trị '{}'. Role: {}, UserId: {}",
                        toolName, userRole, userId);
                return ToolAccessDecision.deny("Từ chối truy cập: Công cụ '" + toolName +
                        "' chứa số liệu nội bộ nhạy cảm và chỉ dành riêng cho Quản trị viên (Admin).");
            }
            return ToolAccessDecision.allow();
        }

        if (PUBLIC_TOOLS.contains(toolName) || TRACK_ORDER_TOOL.equals(toolName)) {
            return ToolAccessDecision.allow();
        }

        log.warn("[SECURITY POLICY] Công cụ '{}' chưa được khai báo trong chính sách bảo mật.", toolName);
        return ToolAccessDecision.deny("Công cụ '" + toolName + "' không được hỗ trợ hoặc bị chặn bởi chính sách an toàn.");
    }

    /**
     * Xác minh quyền sở hữu hoặc mã định danh của đơn hàng khi gọi track_order
     */
    public OrderAccessDecision checkOrderAccess(Order order, Long currentUserId, String userRole, String inputPhone) {
        if (order == null) {
            return OrderAccessDecision.deny("Đơn hàng không tồn tại trong hệ thống.");
        }

        // 1. Quản trị viên có toàn quyền xem chi tiết
        if (isAdmin(userRole)) {
            log.info("[ORDER ACCESS] Admin {} tra cứu đơn #{}", userRole, order.getOrderNumber());
            return OrderAccessDecision.allow(true, "ADMIN");
        }

        // 2. Chủ sở hữu đơn hàng (đã đăng nhập và khớp userId)
        if (currentUserId != null && order.getUserId() != null && order.getUserId().equals(currentUserId)) {
            log.info("[ORDER ACCESS] Chủ sở hữu userId: {} tra cứu đơn #{}", currentUserId, order.getOrderNumber());
            return OrderAccessDecision.allow(true, "OWNER");
        }

        // 3. Khách vãng lai xác minh qua số điện thoại đặt hàng
        if (inputPhone != null && !inputPhone.isBlank() && order.getPhone() != null) {
            String cleanInput = inputPhone.replaceAll("[^0-9]", "");
            String cleanOrderPhone = order.getPhone().replaceAll("[^0-9]", "");
            if (!cleanInput.isBlank() && (cleanOrderPhone.endsWith(cleanInput) || cleanInput.endsWith(cleanOrderPhone))) {
                log.info("[ORDER ACCESS] Khách xác minh thành công SĐT cho đơn #{}", order.getOrderNumber());
                return OrderAccessDecision.allow(false, "GUEST_VERIFIED");
            }
        }

        // 4. Từ chối nếu không thỏa mãn bất kỳ điều kiện nào
        log.warn("[ORDER ACCESS DENIED] Từ chối tra cứu đơn #{}. UserId: {}, Role: {}",
                order.getOrderNumber(), currentUserId, userRole);
        return OrderAccessDecision.deny("Để bảo mật thông tin đơn hàng, vui lòng cung cấp đúng số điện thoại đặt hàng kèm theo mã đơn (ví dụ: 'Tra cứu đơn "
                + order.getOrderNumber() + " số điện thoại 0912xxxxxx').");
    }

    /**
     * Kiểm tra role có phải ADMIN hay không
     */
    public boolean isAdmin(String userRole) {
        if (userRole == null) return false;
        String normalized = userRole.toUpperCase().trim();
        return normalized.contains("ADMIN") || normalized.equals("ROLE_ADMIN");
    }

    /**
     * Kiểm tra xem một tool có phải là tool chỉ dành cho Admin hay không
     */
    public boolean isAdminOnlyTool(String toolName) {
        return toolName != null && ADMIN_TOOLS.contains(toolName);
    }

    /**
     * Nhận diện xem câu hỏi của người dùng có yêu cầu dữ liệu quản trị / nhạy cảm hay không
     * (Doanh thu, tài chính, vốn tồn kho, phân tích đơn hàng, phân tích khách hàng...)
     */
    public boolean isPrivilegedAdminQuery(ChatIntentAnalyzer.AnalysisResult analysis, String userMessage, String lastIntent) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }

        String norm = normalizeText(userMessage);

        // 1. Kiểm tra từ khóa trực tiếp về doanh thu / tài chính
        if (norm.contains("doanh thu") || norm.contains("bao cao doanh thu") || norm.contains("tien ban")
                || norm.contains("doanh so") || norm.contains("loi nhuan") || norm.contains("tai chinh")
                || norm.contains("thong ke doanh thu") || norm.contains("hieu suat san pham")
                || norm.contains("danh muc nao ban chay") || norm.contains("doanh thu theo danh muc")) {
            return true;
        }

        // 2. Kiểm tra từ khóa tồn kho nội bộ / cảnh báo quản trị
        if (norm.contains("canh bao ton kho") || norm.contains("hang sap het") || norm.contains("ton kho thap")
                || norm.contains("sap het hang") || norm.contains("gia tri ton kho") || norm.contains("von luu kho")
                || norm.contains("bao cao ton kho") || norm.contains("hang ton lau") || norm.contains("cham luan chuyen")
                || norm.contains("nhap them bao nhieu") || norm.contains("can nhap them")) {
            return true;
        }

        // 3. Kiểm tra phân tích đơn hàng / AOV / tỷ lệ hủy hoàn
        if (norm.contains("ty le huy") || norm.contains("ty le hoan") || norm.contains("aov")
                || norm.contains("gia tri don trung binh") || norm.contains("phan tich don hang")) {
            return true;
        }

        // 4. Kiểm tra phân tích khách hàng
        if (norm.contains("khach hang moi") || norm.contains("khach quay lai") || norm.contains("ty le giu chan")
                || norm.contains("phan tich khach hang") || norm.contains("ti le mua lai")) {
            return true;
        }

        // 5. Kiểm tra phân tích thắc mắc người dùng
        if (norm.contains("nguoi dung thuong hoi") || norm.contains("hay hoi") || norm.contains("khach hang quan tam")
                || norm.contains("chu de hoi") || norm.contains("nhu cau khach") || norm.contains("thong ke tim kiem")
                || norm.contains("hoi ve van de gi")) {
            return true;
        }

        // 6. Intent phân loại từ ChatIntentAnalyzer
        if (analysis != null && analysis.intent() != null) {
            ChatIntentAnalyzer.Intent it = analysis.intent();
            if (it == ChatIntentAnalyzer.Intent.SALES_ANALYTICS
                    || it == ChatIntentAnalyzer.Intent.INVENTORY_ANALYTICS
                    || it == ChatIntentAnalyzer.Intent.ORDER_ANALYTICS
                    || it == ChatIntentAnalyzer.Intent.CUSTOMER_ANALYTICS
                    || it == ChatIntentAnalyzer.Intent.INQUIRY_ANALYTICS) {
                return true;
            }
        }

        // 7. Ngữ cảnh nối tiếp: Lượt chat trước đó đang hỏi về nghiệp vụ quản trị
        if (lastIntent != null && isPrivilegedIntentName(lastIntent)) {
            // Người dùng bổ sung mốc thời gian hoặc câu trả lời ngắn
            if (norm.matches(".*\\b(thang nay|thang truoc|hom nay|hom qua|tuan nay|tuan truoc|nam nay|quy nay|xem di|cho xem|thong ke di|chi tiet|ok|dung roi|co)\\b.*")
                    || norm.matches(".*\\d{1,2}[/-]\\d{1,2}.*")
                    || norm.length() <= 25) {
                return true;
            }
        }

        return false;
    }

    private boolean isPrivilegedIntentName(String intentName) {
        if (intentName == null) return false;
        return intentName.equals("SALES_ANALYTICS")
                || intentName.equals("INVENTORY_ANALYTICS")
                || intentName.equals("ORDER_ANALYTICS")
                || intentName.equals("CUSTOMER_ANALYTICS")
                || intentName.equals("INQUIRY_ANALYTICS");
    }

    public String getAdminRefusalMessage() {
        return "Xin lỗi bạn, các thông tin về doanh thu, tình hình tài chính và báo cáo quản trị nội bộ chỉ dành riêng cho Quản trị viên hệ thống (Admin). Vui lòng đăng nhập với tài khoản Quản trị viên để tra cứu các thông tin này.";
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
