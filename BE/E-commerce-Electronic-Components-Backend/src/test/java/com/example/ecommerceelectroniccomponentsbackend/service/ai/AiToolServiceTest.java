package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiToolServiceTest {

    @Mock
    private IProductSearchService productSearchService;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ChatToolLogRepository chatToolLogRepository;
    @Mock
    private ProjectPlanningService projectPlanningService;
    @Mock
    private AdminAnalyticsService adminAnalyticsService;
    @Mock
    private com.example.ecommerceelectroniccomponentsbackend.service.IWarrantyService warrantyService;

    private AiToolService aiToolService;

    @BeforeEach
    void setUp() {
        aiToolService = new AiToolService(
                productSearchService,
                productVariantRepository,
                productRepository,
                orderRepository,
                chatToolLogRepository,
                new ObjectMapper(),
                projectPlanningService,
                adminAnalyticsService,
                new AiSecuritySanitizer(),
                new AiToolPolicyService(),
                warrantyService
        );
    }

    @Test
    @DisplayName("Kiểm tra danh sách Tool Definitions đủ 17 công cụ bao gồm nhóm Dashboard, Analytics và Bảo hành")
    void testGetToolDefinitionsContainsAll8Tools() {
        List<Map<String, Object>> tools = aiToolService.getToolDefinitions();
        assertEquals(17, tools.size());

        List<String> names = tools.stream()
                .map(t -> (Map<String, Object>) t.get("function"))
                .map(f -> (String) f.get("name"))
                .toList();

        assertTrue(names.contains("search_products"));
        assertTrue(names.contains("recommend_products"));
        assertTrue(names.contains("compare_products"));
        assertTrue(names.contains("check_stock"));
        assertTrue(names.contains("get_low_stock_products"));
        assertTrue(names.contains("get_top_selling_products"));
        assertTrue(names.contains("get_revenue_report"));
        assertTrue(names.contains("track_order"));
        assertTrue(names.contains("find_project_components"));
        assertTrue(names.contains("check_component_compatibility"));
        assertTrue(names.contains("build_bill_of_materials"));
        assertTrue(names.contains("get_order_analytics"));
        assertTrue(names.contains("get_inventory_report"));
        assertTrue(names.contains("get_product_performance"));
        assertTrue(names.contains("get_customer_analytics"));
        assertTrue(names.contains("get_customer_inquiry_analytics"));
        assertTrue(names.contains("check_warranty"));
    }

    @Test
    @DisplayName("Bảo mật RBAC: Khách thường hoặc Khách vãng lai bị từ chối truy cập 4 tool Dashboard")
    void testAdminAnalyticsRBACProtection() {
        UUID sid = UUID.randomUUID();
        Long uid = 123L;

        Map<String, Object> r1 = aiToolService.getOrderAnalytics("2026-10-01", "2026-10-24", "range", "ROLE_USER", sid, uid);
        assertFalse((Boolean) r1.get("success"));
        assertTrue(((String) r1.get("error")).contains("Từ chối truy cập"));

        Map<String, Object> r2 = aiToolService.getInventoryReport(10, "ROLE_GUEST", sid, uid);
        assertFalse((Boolean) r2.get("success"));
        assertTrue(((String) r2.get("error")).contains("Từ chối truy cập"));

        Map<String, Object> r3 = aiToolService.getProductPerformance(5, "ROLE_USER", sid, uid);
        assertFalse((Boolean) r3.get("success"));
        assertTrue(((String) r3.get("error")).contains("Từ chối truy cập"));

        Map<String, Object> r4 = aiToolService.getCustomerAnalytics("2026-10-01", "2026-10-24", "ROLE_USER", sid, uid);
        assertFalse((Boolean) r4.get("success"));
        assertTrue(((String) r4.get("error")).contains("Từ chối truy cập"));

        Map<String, Object> r5 = aiToolService.getCustomerInquiryAnalytics(5, "ROLE_USER", sid, uid);
        assertFalse((Boolean) r5.get("success"));
        assertTrue(((String) r5.get("error")).contains("Từ chối truy cập"));
    }

    @Test
    @DisplayName("Bảo mật track_order: Admin có quyền xem toàn bộ thông tin đơn hàng")
    void testTrackOrderAdminAccess() {
        Order mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .userId(100L)
                .fullName("Nguyễn Văn A")
                .phone("0987654321")
                .address("123 Đường ABC")
                .ward("Phường 1")
                .province("TP Hồ Chí Minh")
                .status(Order.OrderStatus.SHIPPED)
                .total(new BigDecimal("500000"))
                .build();

        when(orderRepository.findByOrderNumberWithItems("ORD-12345")).thenReturn(Optional.of(mockOrder));

        Map<String, Object> result = aiToolService.trackOrder("ORD-12345", null, "ROLE_ADMIN", 999L, UUID.randomUUID());

        assertTrue((Boolean) result.get("success"));
        assertNull(result.get("error"));
        assertEquals("ORD-12345", result.get("orderNumber"));
        assertEquals("Nguyễn Văn A", result.get("customerName")); // Admin thấy tên đầy đủ
    }

    @Test
    @DisplayName("Bảo mật track_order: Chủ đơn hàng (Owner) có quyền xem đơn của mình")
    void testTrackOrderOwnerAccess() {
        Order mockOrder = Order.builder()
                .id(2L)
                .orderNumber("ORD-88888")
                .userId(50L)
                .fullName("Trần Thị B")
                .phone("0912345678")
                .address("456 Đường XYZ")
                .ward("Phường Tân Định")
                .province("Hà Nội")
                .status(Order.OrderStatus.CONFIRMED)
                .total(new BigDecimal("300000"))
                .build();

        when(orderRepository.findByOrderNumberWithItems("ORD-88888")).thenReturn(Optional.of(mockOrder));

        // User id = 50L (chính là chủ đơn)
        Map<String, Object> result = aiToolService.trackOrder("ORD-88888", null, "ROLE_USER", 50L, UUID.randomUUID());

        assertTrue((Boolean) result.get("success"));
        assertNull(result.get("error"));
        assertEquals("Trần Thị B", result.get("customerName"));
    }

    @Test
    @DisplayName("Bảo mật track_order: Khách vãng lai cung cấp đúng số điện thoại được xem dữ liệu đã che (Masked)")
    void testTrackOrderGuestWithCorrectPhone() {
        Order mockOrder = Order.builder()
                .id(3L)
                .orderNumber("ORD-77777")
                .userId(null)
                .fullName("Lê Hoàng Nam")
                .phone("0905123456")
                .address("789 Hẻm 12")
                .ward("Phường Bến Nghé")
                .province("TP Hồ Chí Minh")
                .status(Order.OrderStatus.DELIVERED)
                .total(new BigDecimal("1200000"))
                .build();

        when(orderRepository.findByOrderNumberWithItems("ORD-77777")).thenReturn(Optional.of(mockOrder));

        // Guest không có userId, nhưng cung cấp đúng số điện thoại
        Map<String, Object> result = aiToolService.trackOrder("ORD-77777", "0905123456", "ROLE_GUEST", null, UUID.randomUUID());

        assertTrue((Boolean) result.get("success"));
        assertNull(result.get("error"));
        // Tên bị che
        assertTrue(((String) result.get("customerName")).contains("***"));
        // SĐT bị che
        assertTrue(((String) result.get("maskedPhone")).contains("****"));
        // Địa chỉ chỉ giữ phường, tỉnh (không lộ số nhà chi tiết)
        assertFalse(((String) result.get("address")).contains("789 Hẻm 12"));
        assertTrue(((String) result.get("address")).contains("Phường Bến Nghé"));
    }

    @Test
    @DisplayName("Bảo mật track_order: Khách vãng lai không cung cấp hoặc sai SĐT sẽ bị từ chối")
    void testTrackOrderGuestWithoutPhoneDenied() {
        Order mockOrder = Order.builder()
                .id(4L)
                .orderNumber("ORD-99999")
                .userId(10L)
                .fullName("Bí Mật Khách Hàng")
                .phone("0918999888")
                .build();

        when(orderRepository.findByOrderNumberWithItems("ORD-99999")).thenReturn(Optional.of(mockOrder));

        // Guest không gửi SĐT
        Map<String, Object> result = aiToolService.trackOrder("ORD-99999", null, "ROLE_GUEST", null, UUID.randomUUID());

        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        assertTrue(((String) result.get("error")).contains("số điện thoại"));
    }

    @Test
    @DisplayName("Tool 17: check_warranty tra cứu thông tin bảo hành chính xác")
    void testCheckWarrantySuccess() {
        com.example.ecommerceelectroniccomponentsbackend.dto.warranty.WarrantyLookupResponseDTO mockRes =
                com.example.ecommerceelectroniccomponentsbackend.dto.warranty.WarrantyLookupResponseDTO.builder()
                        .serialNumber("SN-202610-A1B2C3D4")
                        .productName("Kit STM32 Blue Pill")
                        .status("ACTIVE")
                        .isExpired(false)
                        .daysRemaining(120)
                        .warrantyMonths(6)
                        .activatedAt(java.time.LocalDateTime.now().minusMonths(2))
                        .expiresAt(java.time.LocalDateTime.now().plusMonths(4))
                        .build();

        when(warrantyService.lookupWarranty("SN-202610-A1B2C3D4", null)).thenReturn(mockRes);

        Map<String, Object> result = aiToolService.checkWarranty("SN-202610-A1B2C3D4", null, UUID.randomUUID(), 1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals("WARRANTY_LOOKUP", result.get("type"));
        assertEquals("SN-202610-A1B2C3D4", result.get("serialNumber"));
        assertEquals("Kit STM32 Blue Pill", result.get("productName"));
        assertFalse((Boolean) result.get("isExpired"));
        assertEquals(120L, result.get("daysRemaining"));
    }
}
