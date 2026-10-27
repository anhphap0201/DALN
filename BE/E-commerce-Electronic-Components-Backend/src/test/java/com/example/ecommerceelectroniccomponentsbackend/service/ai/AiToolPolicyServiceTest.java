package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiToolPolicyServiceTest {

    private AiToolPolicyService policyService;

    @BeforeEach
    void setUp() {
        policyService = new AiToolPolicyService();
    }

    @Test
    @DisplayName("Admin Tools: Cho phép Quản trị viên (ROLE_ADMIN) truy cập toàn bộ 6 tool nội bộ")
    void testAdminTools_AllowedForAdmin() {
        List<String> adminTools = List.of(
                "get_low_stock_products",
                "get_revenue_report",
                "get_order_analytics",
                "get_inventory_report",
                "get_product_performance",
                "get_customer_analytics",
                "get_customer_inquiry_analytics"
        );

        for (String tool : adminTools) {
            var decisionRoleAdmin = policyService.checkToolAccess(tool, "ROLE_ADMIN", 1L);
            assertTrue(decisionRoleAdmin.isAllowed(), "Tool " + tool + " phải được cho phép với ROLE_ADMIN");

            var decisionAdmin = policyService.checkToolAccess(tool, "ADMIN", 1L);
            assertTrue(decisionAdmin.isAllowed(), "Tool " + tool + " phải được cho phép với ADMIN");
        }
    }

    @Test
    @DisplayName("Admin Tools: Từ chối cứng đối với User thường và Khách vãng lai")
    void testAdminTools_DeniedForUserAndGuest() {
        List<String> adminTools = List.of(
                "get_low_stock_products",
                "get_revenue_report",
                "get_order_analytics",
                "get_inventory_report",
                "get_product_performance",
                "get_customer_analytics",
                "get_customer_inquiry_analytics"
        );

        for (String tool : adminTools) {
            var decisionUser = policyService.checkToolAccess(tool, "ROLE_USER", 2L);
            assertFalse(decisionUser.isAllowed(), "Tool " + tool + " phải bị từ chối với ROLE_USER");
            assertTrue(decisionUser.getReason().contains("Từ chối truy cập"));

            var decisionGuest = policyService.checkToolAccess(tool, "ROLE_GUEST", null);
            assertFalse(decisionGuest.isAllowed(), "Tool " + tool + " phải bị từ chối với ROLE_GUEST");

            var decisionNull = policyService.checkToolAccess(tool, null, null);
            assertFalse(decisionNull.isAllowed(), "Tool " + tool + " phải bị từ chối với role null");
        }
    }

    @Test
    @DisplayName("Public Tools: Cho phép mọi đối tượng (User, Guest, null role) sử dụng")
    void testPublicTools_AllowedForAll() {
        List<String> publicTools = List.of(
                "search_products",
                "recommend_products",
                "compare_products",
                "check_stock",
                "get_top_selling_products",
                "find_project_components",
                "check_component_compatibility",
                "build_bill_of_materials"
        );

        for (String tool : publicTools) {
            assertTrue(policyService.checkToolAccess(tool, "ROLE_GUEST", null).isAllowed());
            assertTrue(policyService.checkToolAccess(tool, "ROLE_USER", 10L).isAllowed());
            assertTrue(policyService.checkToolAccess(tool, null, null).isAllowed());
        }
    }

    @Test
    @DisplayName("Order Security: Admin có toàn quyền tra cứu đơn và xem chi tiết (fullDetails = true)")
    void testOrderAccess_Admin() {
        Order order = Order.builder()
                .orderNumber("ORD-999")
                .userId(50L)
                .phone("0987654321")
                .build();

        var decision = policyService.checkOrderAccess(order, 1L, "ROLE_ADMIN", null);
        assertTrue(decision.isAllowed());
        assertTrue(decision.isFullDetails());
        assertEquals("ADMIN", decision.getAccessType());
    }

    @Test
    @DisplayName("Order Security: Chủ sở hữu đơn hàng (đã đăng nhập) được xem chi tiết (fullDetails = true)")
    void testOrderAccess_Owner() {
        Long ownerId = 50L;
        Order order = Order.builder()
                .orderNumber("ORD-100")
                .userId(ownerId)
                .phone("0912345678")
                .build();

        var decision = policyService.checkOrderAccess(order, ownerId, "ROLE_USER", null);
        assertTrue(decision.isAllowed());
        assertTrue(decision.isFullDetails());
        assertEquals("OWNER", decision.getAccessType());
    }

    @Test
    @DisplayName("Order Security: Khách vãng lai xác minh đúng SĐT được xem đơn nhưng ẩn chi tiết (fullDetails = false)")
    void testOrderAccess_GuestWithMatchingPhone() {
        Order order = Order.builder()
                .orderNumber("ORD-200")
                .userId(null)
                .phone("0988776655")
                .build();

        var decision = policyService.checkOrderAccess(order, null, "ROLE_GUEST", "0988776655");
        assertTrue(decision.isAllowed());
        assertFalse(decision.isFullDetails());
        assertEquals("GUEST_VERIFIED", decision.getAccessType());
    }

    @Test
    @DisplayName("Order Security: Khách không nhập SĐT hoặc nhập sai SĐT bị từ chối")
    void testOrderAccess_Denied() {
        Order order = Order.builder()
                .orderNumber("ORD-300")
                .userId(99L)
                .phone("0988112233")
                .build();

        // Khách không nhập SĐT
        var decisionNoPhone = policyService.checkOrderAccess(order, null, "ROLE_GUEST", null);
        assertFalse(decisionNoPhone.isAllowed());
        assertTrue(decisionNoPhone.getReason().contains("cung cấp đúng số điện thoại"));

        // Khách nhập sai SĐT
        var decisionWrongPhone = policyService.checkOrderAccess(order, null, "ROLE_GUEST", "0911000000");
        assertFalse(decisionWrongPhone.isAllowed());

        // User khác cố tình tra cứu đơn của người khác không có SĐT
        var decisionOtherUser = policyService.checkOrderAccess(order, 88L, "ROLE_USER", null);
        assertFalse(decisionOtherUser.isAllowed());
    }

    @Test
    @DisplayName("Công cụ lạ không có trong Policy Registry bị chặn an toàn")
    void testUnknownTool_Denied() {
        var decision = policyService.checkToolAccess("drop_database_users", "ROLE_ADMIN", 1L);
        assertFalse(decision.isAllowed());
    }
}
