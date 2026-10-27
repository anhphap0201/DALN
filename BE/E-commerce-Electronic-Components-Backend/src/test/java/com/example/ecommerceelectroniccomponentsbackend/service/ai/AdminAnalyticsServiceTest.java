package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private com.example.ecommerceelectroniccomponentsbackend.repository.SearchHistoryRepository searchHistoryRepository;
    @Mock
    private com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository chatToolLogRepository;
    @Mock
    private com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository chatMessageRepository;

    private AdminAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AdminAnalyticsService(
                orderRepository,
                productRepository,
                productVariantRepository,
                userRepository,
                searchHistoryRepository,
                chatToolLogRepository,
                chatMessageRepository
        );
    }

    @Test
    @DisplayName("getOrderAnalytics: Tính toán chính xác AOV, tỷ lệ hủy và doanh thu giao thành công")
    void testGetOrderAnalyticsCalculations() {
        Order o1 = Order.builder()
                .id(1L)
                .status(Order.OrderStatus.DELIVERED)
                .total(new BigDecimal("400000"))
                .paymentMethod("VNPAY")
                .createdAt(LocalDateTime.now())
                .build();

        Order o2 = Order.builder()
                .id(2L)
                .status(Order.OrderStatus.DELIVERED)
                .total(new BigDecimal("600000"))
                .paymentMethod("COD")
                .createdAt(LocalDateTime.now())
                .build();

        Order o3 = Order.builder()
                .id(3L)
                .status(Order.OrderStatus.CANCELLED)
                .total(new BigDecimal("200000"))
                .paymentMethod("COD")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(o1, o2, o3))
                .thenReturn(List.of()); // prev period

        Map<String, Object> result = analyticsService.getOrderAnalytics("2026-10-01", "2026-10-24", "range");

        assertEquals(3L, result.get("totalOrders"));
        assertEquals(2L, result.get("deliveredOrders"));
        assertEquals(1L, result.get("cancelledOrders"));
        assertEquals(new BigDecimal("1000000"), result.get("deliveredRevenue"));
        assertEquals(new BigDecimal("500000"), result.get("averageOrderValue")); // 1.000.000 / 2 = 500.000
        assertEquals("33.3%", result.get("cancellationRate"));
    }

    @Test
    @DisplayName("getInventoryReport: Tính toán chính xác giá trị tồn kho và phát hiện hàng cạn kho/hàng tồn chậm")
    void testGetInventoryReportCalculations() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Arduino Uno");
        p.setSoldQuantity(120);

        Product slowP = new Product();
        slowP.setId(2L);
        slowP.setName("Cảm biến hiếm");
        slowP.setSoldQuantity(2);

        ProductVariant v1 = new ProductVariant();
        v1.setId(10L);
        v1.setProduct(p);
        v1.setPrice(new BigDecimal("100000"));
        v1.setInStock(5);
        v1.setVariantName("SMD");

        ProductVariant v2 = new ProductVariant();
        v2.setId(11L);
        v2.setProduct(slowP);
        v2.setPrice(new BigDecimal("50000"));
        v2.setInStock(30);
        v2.setVariantName("Standard");

        ProductVariant v3 = new ProductVariant();
        v3.setId(12L);
        v3.setProduct(p);
        v3.setPrice(new BigDecimal("120000"));
        v3.setInStock(0);
        v3.setVariantName("DIP");

        when(productVariantRepository.findAll()).thenReturn(List.of(v1, v2, v3));

        Map<String, Object> report = analyticsService.getInventoryReport(10);

        assertEquals(3, report.get("totalSkus"));
        assertEquals(35L, report.get("totalStockUnits")); // 5 + 30 + 0
        // Value: 5 * 100k + 30 * 50k = 500k + 1.500k = 2.000.000
        assertEquals(new BigDecimal("2000000"), report.get("totalInventoryValue"));
        assertEquals(1, report.get("outOfStockCount")); // v3
        assertEquals(1, report.get("lowStockCount"));   // v1 (5 <= 10)
        assertEquals(1, report.get("slowMovingCount")); // v2 (stock 30 >= 20, sold 2 < 10)
    }

    @Test
    @DisplayName("getCustomerAnalytics: Tính toán số khách mới và tỷ lệ mua lại")
    void testGetCustomerAnalytics() {
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(15L);
        when(userRepository.count()).thenReturn(150L);

        Order o1 = Order.builder().userId(10L).total(new BigDecimal("100000")).build();
        Order o2 = Order.builder().userId(10L).total(new BigDecimal("200000")).build(); // User 10 mua 2 lần
        Order o3 = Order.builder().userId(11L).total(new BigDecimal("150000")).build(); // User 11 mua 1 lần

        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(o1, o2, o3));

        Map<String, Object> result = analyticsService.getCustomerAnalytics("2026-10-01", "2026-10-24");

        assertEquals(150L, result.get("totalRegisteredUsers"));
        assertEquals(15L, result.get("newRegisteredUsers"));
        assertEquals(2L, result.get("activeOrderingUsers")); // User 10 & User 11
        assertEquals(1L, result.get("repeatCustomers"));     // User 10
        assertEquals("50.0%", result.get("repeatCustomerRate")); // 1 / 2 = 50%
    }

    @Test
    @DisplayName("getCustomerInquiryAnalytics: Phân tích 3 chiều chủ đề chat, từ khóa tìm kiếm và nhu cầu chưa đáp ứng")
    void testGetCustomerInquiryAnalytics() {
        when(chatToolLogRepository.findTop100ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(searchHistoryRepository.findTopPopularKeywords(any())).thenReturn(List.of());
        when(searchHistoryRepository.findTopZeroResultKeywords(any())).thenReturn(List.of());

        Map<String, Object> result = analyticsService.getCustomerInquiryAnalytics(5);
        assertNotNull(result);
        assertTrue(result.containsKey("topProjectTopics"));
        assertTrue(result.containsKey("popularSearches"));
        assertTrue(result.containsKey("unmetDemand"));
        assertNotNull(result.get("summary"));
    }
}
