package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPlanningServiceTest {

    @Mock
    private IProductSearchService productSearchService;

    private ProjectPlanningService planningService;

    @BeforeEach
    void setUp() {
        planningService = new ProjectPlanningService(productSearchService);
    }

    private ProductWithVariantsDTO createMockProduct(Long id, String name, BigDecimal minPrice, int totalStock) {
        ProductWithVariantsDTO dto = new ProductWithVariantsDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setMinPrice(minPrice);
        dto.setTotalStock(totalStock);
        return dto;
    }

    @Test
    @DisplayName("findProjectComponents: Trả về danh sách vai trò linh kiện và phân biệt hàng có sẵn vs hàng ngoài")
    void testFindProjectComponentsForDrone() {
        ProductWithVariantsDTO mockLm2596 = createMockProduct(10L, "Module Buck LM2596 DC-DC", new BigDecimal("25000"), 50);

        when(productSearchService.searchProductsByName("lm2596")).thenReturn(List.of(mockLm2596));
        when(productSearchService.searchProductsByName("frame drone")).thenReturn(List.of());

        Map<String, Object> result = planningService.findProjectComponents("mini_drone", List.of("frame", "power_module"), Map.of());

        assertEquals("mini_drone", result.get("projectType"));
        assertEquals(2, result.get("totalRolesExamined"));
        assertEquals(1, result.get("rolesAvailableInShop"));

        @SuppressWarnings("unchecked")
        Map<String, Object> roles = (Map<String, Object>) result.get("roles");
        assertNotNull(roles);

        @SuppressWarnings("unchecked")
        Map<String, Object> frameRole = (Map<String, Object>) roles.get("frame");
        assertFalse((Boolean) frameRole.get("availableInShop"));
        assertNotNull(frameRole.get("externalAdvice"));

        @SuppressWarnings("unchecked")
        Map<String, Object> powerRole = (Map<String, Object>) roles.get("power_module");
        assertTrue((Boolean) powerRole.get("availableInShop"));
    }

    @Test
    @DisplayName("checkCompatibility: Phát hiện xung đột kích thước khung 2 inch với cánh 5 inch")
    void testCheckCompatibilityFramePropMismatch() {
        List<Map<String, Object>> components = List.of(
                Map.of("role", "frame", "name", "Khung drone 2 inch mini whoop"),
                Map.of("role", "propeller", "name", "Cánh quạt 5 inch 3 lá"),
                Map.of("role", "battery", "name", "Pin LiPo 2S 450mAh")
        );

        Map<String, Object> result = planningService.checkCompatibility("mini_drone", components);

        assertFalse((Boolean) result.get("compatible"));
        assertTrue((Integer) result.get("issueCount") > 0);

        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.stream().anyMatch(i -> i.contains("Xung đột kích thước")));
    }

    @Test
    @DisplayName("checkCompatibility: Phát hiện xung đột điện áp Pin 4S với Motor 4500KV")
    void testCheckCompatibilityVoltageKvMismatch() {
        List<Map<String, Object>> components = List.of(
                Map.of("role", "frame", "name", "Khung drone 3 inch"),
                Map.of("role", "motor", "name", "Động cơ brushless 1104 4500KV"),
                Map.of("role", "battery", "name", "Pin LiPo 4S 850mAh")
        );

        Map<String, Object> result = planningService.checkCompatibility("mini_drone", components);

        assertFalse((Boolean) result.get("compatible"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.stream().anyMatch(i -> i.contains("Xung đột điện áp") && i.contains("4S")));
    }

    @Test
    @DisplayName("buildBillOfMaterials: Tự động tổng hợp chi phí shop và chi phí ngoài hoàn chỉnh")
    void testBuildBillOfMaterialsRobot() {
        ProductWithVariantsDTO mockUno = createMockProduct(1L, "Arduino Uno R3", new BigDecimal("150000"), 20);
        ProductWithVariantsDTO mockHcsr04 = createMockProduct(2L, "HC-SR04 - Cảm biến siêu âm", new BigDecimal("35000"), 15);

        when(productSearchService.searchProductsByName(anyString())).thenReturn(List.of());
        when(productSearchService.searchProductsByName("arduino uno")).thenReturn(List.of(mockUno));
        when(productSearchService.searchProductsByName("hc-sr04")).thenReturn(List.of(mockHcsr04));

        Map<String, Object> bom = planningService.buildBillOfMaterials("obstacle_avoiding_robot", Map.of());

        assertEquals("obstacle_avoiding_robot", bom.get("projectType"));
        assertTrue((Integer) bom.get("shopItemCount") >= 2);

        BigDecimal shopTotal = (BigDecimal) bom.get("shopTotal");
        BigDecimal grandTotal = (BigDecimal) bom.get("grandTotal");

        assertTrue(shopTotal.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(grandTotal.compareTo(shopTotal) >= 0);
        assertNotNull(bom.get("formattedGrandTotal"));
    }
}
