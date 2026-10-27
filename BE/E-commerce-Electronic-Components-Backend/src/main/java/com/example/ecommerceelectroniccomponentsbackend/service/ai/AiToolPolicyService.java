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
}
