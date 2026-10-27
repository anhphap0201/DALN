package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectPlanningService {

    private final IProductSearchService productSearchService;

    public record ProjectRoleDefinition(
            String role,
            String displayName,
            String searchKeyword,
            int defaultQuantity,
            String necessity, // "REQUIRED" | "OPTIONAL"
            String externalEstimateDesc,
            BigDecimal externalEstimatePrice
    ) {}

    /**
     * Định nghĩa danh mục vai trò linh kiện theo từng loại dự án
     */
    public List<ProjectRoleDefinition> getProjectRoles(String projectType) {
        String cleanType = projectType != null ? projectType.toLowerCase(Locale.ROOT) : "";
        if (cleanType.contains("drone") || cleanType.contains("quadcopter")) {
            return List.of(
                    new ProjectRoleDefinition("frame", "Khung Drone", "frame drone", 1, "REQUIRED", "Khung Carbon 3 inch", new BigDecimal("250000")),
                    new ProjectRoleDefinition("motor", "Động cơ Brushless", "motor brushless", 4, "REQUIRED", "4x Động cơ không chổi than 1408/1507", new BigDecimal("1120000")),
                    new ProjectRoleDefinition("esc", "Mạch điều tốc ESC", "esc 4 in 1", 1, "REQUIRED", "Mạch ESC 4-in-1 20A-30A BLHeli_S", new BigDecimal("350000")),
                    new ProjectRoleDefinition("propeller", "Cánh quạt (CW/CCW)", "canh quat drone", 4, "REQUIRED", "Bộ 4 cánh quạt 3 inch", new BigDecimal("60000")),
                    new ProjectRoleDefinition("flight_controller", "Mạch điều khiển bay / Cảm biến góc", "mpu6050", 1, "REQUIRED", "Mạch điều khiển bay FC F4/F7", new BigDecimal("450000")),
                    new ProjectRoleDefinition("power_module", "Mạch nguồn / Hạ áp", "lm2596", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("communication", "Mạch thu sóng RF / Điều khiển", "nrf24l01", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("battery", "Pin LiPo", "pin lipo", 1, "REQUIRED", "Pin LiPo 3S 850mAh - 1100mAh", new BigDecimal("280000")),
                    new ProjectRoleDefinition("battery_charger", "Mạch sạc pin", "tp4056", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("fpv_camera", "Camera FPV & VTX", "camera fpv", 1, "OPTIONAL", "Bộ Camera FPV + VTX phát hình 5.8GHz", new BigDecimal("450000"))
            );
        } else if (cleanType.contains("obstacle") || cleanType.contains("tranh vat can")) {
            return List.of(
                    new ProjectRoleDefinition("microcontroller", "Mạch vi điều khiển trung tâm", "arduino uno", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("ultrasonic_sensor", "Cảm biến siêu âm đo khoảng cách", "hc-sr04", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("servo", "Động cơ Servo xoay cảm biến", "sg90", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("motor_driver", "Mạch cầu H điều khiển động cơ", "l298n", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("motors", "Động cơ giảm tốc kèm bánh xe", "dong co giam toc", 2, "REQUIRED", null, null),
                    new ProjectRoleDefinition("power_module", "Module hạ áp nguồn", "lm2596", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("display", "Màn hình hiển thị trạng thái", "oled", 1, "OPTIONAL", null, null),
                    new ProjectRoleDefinition("charger", "Mạch sạc pin Lithium", "tp4056", 1, "OPTIONAL", null, null)
            );
        } else if (cleanType.contains("line") || cleanType.contains("do line") || cleanType.contains("do duong")) {
            return List.of(
                    new ProjectRoleDefinition("microcontroller", "Mạch vi điều khiển", "arduino nano", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("motor_driver", "Mạch điều khiển động cơ", "l298n", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("motors", "Động cơ giảm tốc", "dong co giam toc", 2, "REQUIRED", null, null),
                    new ProjectRoleDefinition("power_module", "Module nguồn hạ áp", "lm2596", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("line_sensor", "Cảm biến hồng ngoại dò line", "tcrt5000", 1, "REQUIRED", "Thanh 5 cảm biến dò line TCRT5000", new BigDecimal("45000")),
                    new ProjectRoleDefinition("chassis", "Khung xe 2WD", "khung xe 2wd", 1, "REQUIRED", "Bộ khung xe robot 2WD mica", new BigDecimal("70000"))
            );
        } else {
            // Mặc định: Trạm IoT / Dự án thông minh
            return List.of(
                    new ProjectRoleDefinition("microcontroller", "Mạch vi điều khiển Wi-Fi/Bluetooth", "esp32", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("temp_humidity_sensor", "Cảm biến nhiệt độ & độ ẩm", "dht22", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("pressure_sensor", "Cảm biến áp suất", "bmp280", 1, "OPTIONAL", null, null),
                    new ProjectRoleDefinition("gas_sensor", "Cảm biến khí gas / khói", "mq-2", 1, "OPTIONAL", null, null),
                    new ProjectRoleDefinition("display", "Màn hình hiển thị", "oled", 1, "REQUIRED", null, null),
                    new ProjectRoleDefinition("power_module", "Module cấp nguồn", "lm2596", 1, "REQUIRED", null, null)
            );
        }
    }

    /**
     * Tool 1 Implementation: Tìm linh kiện theo vai trò trong dự án
     */
    public Map<String, Object> findProjectComponents(String projectType, List<String> requestedRoles, Map<String, Object> constraints) {
        List<ProjectRoleDefinition> allRoles = getProjectRoles(projectType);
        Map<String, Object> resultByRole = new LinkedHashMap<>();
        int matchedCount = 0;

        for (ProjectRoleDefinition roleDef : allRoles) {
            if (requestedRoles != null && !requestedRoles.isEmpty() && !requestedRoles.contains(roleDef.role())) {
                continue;
            }

            List<ProductWithVariantsDTO> matches = productSearchService.searchProductsByName(roleDef.searchKeyword());
            List<Map<String, Object>> candidateList = new ArrayList<>();

            for (ProductWithVariantsDTO p : matches) {
                if (p.getTotalStock() != null && p.getTotalStock() > 0) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    BigDecimal price = p.getMinDiscountPrice() != null ? p.getMinDiscountPrice() : p.getMinPrice();
                    item.put("price", price);
                    item.put("formattedPrice", formatCurrency(price));
                    item.put("stock", p.getTotalStock());
                    candidateList.add(item);
                }
            }

            Map<String, Object> roleResult = new LinkedHashMap<>();
            roleResult.put("roleName", roleDef.displayName());
            roleResult.put("necessity", roleDef.necessity());
            roleResult.put("defaultQuantity", roleDef.defaultQuantity());
            roleResult.put("availableInShop", !candidateList.isEmpty());
            roleResult.put("candidates", candidateList);

            if (candidateList.isEmpty() && roleDef.externalEstimateDesc() != null) {
                roleResult.put("externalAdvice", roleDef.externalEstimateDesc());
                roleResult.put("externalEstimatePrice", roleDef.externalEstimatePrice());
            }

            if (!candidateList.isEmpty()) matchedCount++;
            resultByRole.put(roleDef.role(), roleResult);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projectType", projectType);
        data.put("totalRolesExamined", resultByRole.size());
        data.put("rolesAvailableInShop", matchedCount);
        data.put("roles", resultByRole);
        return data;
    }

    /**
     * Tool 2 Implementation: Kiểm tra tính tương thích giữa các linh kiện
     */
    public Map<String, Object> checkCompatibility(String projectType, List<Map<String, Object>> components) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        String cleanType = projectType != null ? projectType.toLowerCase(Locale.ROOT) : "";

        if (cleanType.contains("drone") || cleanType.contains("quadcopter")) {
            checkDroneCompatibility(components, issues, warnings, recommendations);
        } else if (cleanType.contains("robot") || cleanType.contains("xe")) {
            checkRobotCompatibility(components, issues, warnings, recommendations);
        } else {
            checkGenericIotCompatibility(components, issues, warnings, recommendations);
        }

        boolean compatible = issues.isEmpty();

        data.put("projectType", projectType);
        data.put("compatible", compatible);
        data.put("issueCount", issues.size());
        data.put("warningCount", warnings.size());
        data.put("issues", issues);
        data.put("warnings", warnings);
        data.put("recommendations", recommendations);

        String summary = compatible
                ? "Các linh kiện hoàn toàn tương thích về mặt điện áp, giao thức và thông số hoạt động."
                : String.format("Phát hiện %d điểm xung đột kỹ thuật cần điều chỉnh trước khi cấp nguồn.", issues.size());
        data.put("summary", summary);

        return data;
    }

    private void checkDroneCompatibility(List<Map<String, Object>> components, List<String> issues, List<String> warnings, List<String> recommendations) {
        String frameSize = "3 inch";
        String batteryVoltage = "3S";
        String propSize = "3 inch";
        int motorKv = 2400;

        if (components != null) {
            for (var c : components) {
                String role = String.valueOf(c.getOrDefault("role", "")).toLowerCase(Locale.ROOT);
                String text = (c.getOrDefault("name", "") + " " + c.getOrDefault("specs", "")).toLowerCase(Locale.ROOT);

                if (role.contains("frame") || text.contains("khung")) {
                    if (text.contains("2 inch") || text.contains("2inch")) frameSize = "2 inch";
                    else if (text.contains("5 inch") || text.contains("5inch")) frameSize = "5 inch";
                    else if (text.contains("3 inch") || text.contains("3inch")) frameSize = "3 inch";
                }
                if (role.contains("prop") || text.contains("canh")) {
                    if (text.contains("5 inch") || text.contains("5inch")) propSize = "5 inch";
                    else if (text.contains("3 inch") || text.contains("3inch")) propSize = "3 inch";
                    else if (text.contains("2 inch") || text.contains("2inch")) propSize = "2 inch";
                }
                if (role.contains("battery") || text.contains("pin") || text.contains("lipo")) {
                    if (text.contains("4s")) batteryVoltage = "4S";
                    else if (text.contains("3s")) batteryVoltage = "3S";
                    else if (text.contains("2s")) batteryVoltage = "2S";
                }
                if (role.contains("motor") || text.contains("dong co")) {
                    if (text.contains("4500kv") || text.contains("4000kv") || text.contains("5000kv")) motorKv = 4500;
                    else if (text.contains("1400kv") || text.contains("1800kv")) motorKv = 1800;
                }
            }
        }

        // 1. Kiểm tra tỷ lệ kích thước khung và cánh
        if ("2 inch".equals(frameSize) && ("3 inch".equals(propSize) || "5 inch".equals(propSize))) {
            issues.add("Xung đột kích thước: Khung drone 2 inch không thể lắp vừa cánh quạt 3 inch hoặc 5 inch (cánh sẽ chạm thân và quẹt nhau).");
        }

        // 2. Kiểm tra điện áp Pin và KV Động cơ
        if ("4S".equals(batteryVoltage) && motorKv >= 4000) {
            issues.add("Xung đột điện áp: Pin 4S (14.8V) cấp vào motor tốc độ cao " + motorKv + "KV sẽ gây quá nhiệt và nguy cơ cháy cuộn dây hoặc nổ ESC.");
            recommendations.add("Nếu dùng motor KV cao > 4000KV, nên hạ pin xuống loại 2S hoặc 3S (7.4V - 11.1V).");
        } else if ("2S".equals(batteryVoltage) && motorKv <= 1800) {
            warnings.add("Cảnh báo lực nâng yếu: Pin 2S với motor " + motorKv + "KV có thể không đủ lực nâng cho quadcopter khi mang theo tải.");
        }

        // 3. Khuyến nghị chuẩn giao thức
        recommendations.add("Đảm bảo mạch điều tốc ESC hỗ trợ giao thức DShot300/DShot600 để đồng bộ xung tốc độ cao với Flight Controller.");
        recommendations.add("Nên sử dụng đầu nối chuẩn XT30 cho dòng pin 2S-3S hoặc XT60 cho dòng pin 4S.");
    }

    private void checkRobotCompatibility(List<Map<String, Object>> components, List<String> issues, List<String> warnings, List<String> recommendations) {
        warnings.add("Lưu ý sụt áp Driver L298N: Mạch cầu H L298N bị sụt áp khoảng 1.5V - 2V qua cặp transistor BJT nội bộ. Để động cơ 6V đạt lực kéo khỏe, nên cấp nguồn vào chân 12V của L298N từ 7.4V (2 pin 18650) trở lên.");
        recommendations.add("Chân Echo của cảm biến HC-SR04 trả về mức logic 5V. Nếu dùng với Arduino Uno thì cắm trực tiếp được; nếu dùng với ESP32 thì bắt buộc qua cầu phân áp 1kΩ - 2kΩ để tránh hỏng chân GPIO.");
    }

    private void checkGenericIotCompatibility(List<Map<String, Object>> components, List<String> issues, List<String> warnings, List<String> recommendations) {
        recommendations.add("Các cảm biến I2C (như OLED 0.96 và BMP280) có thể dùng chung 2 dây SDA và SCL của ESP32/Arduino nhờ khác địa chỉ phần cứng (OLED: 0x3C, BMP280: 0x76).");
        recommendations.add("Module Buck LM2596 cần được điều chỉnh biến trở đo ngõ ra đúng 5.0V bằng đồng hồ vạn năng trước khi cấp vào chân 5V của vi điều khiển.");
    }

    /**
     * Tool 3 Implementation: Tạo bảng dự toán BOM (Bill of Materials) hoàn chỉnh
     */
    public Map<String, Object> buildBillOfMaterials(String projectType, Map<String, Object> constraints) {
        List<ProjectRoleDefinition> roles = getProjectRoles(projectType);

        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> missingItems = new ArrayList<>();
        BigDecimal shopTotal = BigDecimal.ZERO;
        BigDecimal externalEstimatedTotal = BigDecimal.ZERO;

        for (ProjectRoleDefinition roleDef : roles) {
            List<ProductWithVariantsDTO> matches = productSearchService.searchProductsByName(roleDef.searchKeyword());
            ProductWithVariantsDTO selectedProduct = null;

            for (ProductWithVariantsDTO p : matches) {
                if (p.getTotalStock() != null && p.getTotalStock() >= roleDef.defaultQuantity()) {
                    selectedProduct = p;
                    break;
                }
            }

            if (selectedProduct != null) {
                BigDecimal unitPrice = selectedProduct.getMinDiscountPrice() != null
                        ? selectedProduct.getMinDiscountPrice() : selectedProduct.getMinPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(roleDef.defaultQuantity()));
                shopTotal = shopTotal.add(subtotal);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("role", roleDef.role());
                item.put("roleName", roleDef.displayName());
                item.put("productId", selectedProduct.getId());
                Long variantId = null;
                if (selectedProduct.getVariants() != null && !selectedProduct.getVariants().isEmpty()) {
                    variantId = selectedProduct.getVariants().stream()
                            .filter(v -> v.getInStock() != null && v.getInStock() > 0)
                            .map(com.example.ecommerceelectroniccomponentsbackend.dto.ProductVariantDTO::getId)
                            .findFirst()
                            .orElse(selectedProduct.getVariants().get(0).getId());
                }
                item.put("variantId", variantId);
                item.put("productName", selectedProduct.getName());
                item.put("quantity", roleDef.defaultQuantity());
                item.put("unitPrice", unitPrice);
                item.put("formattedUnitPrice", formatCurrency(unitPrice));
                item.put("subtotal", subtotal);
                item.put("formattedSubtotal", formatCurrency(subtotal));
                item.put("stock", selectedProduct.getTotalStock());
                item.put("source", "ECSHOP_STORE");
                item.put("reason", "Linh kiện sẵn có trong kho, đáp ứng tiêu chuẩn kỹ thuật");
                items.add(item);
            } else {
                // Linh kiện shop chưa bán hoặc tạm hết hàng
                BigDecimal extPrice = roleDef.externalEstimatePrice() != null ? roleDef.externalEstimatePrice() : BigDecimal.ZERO;
                externalEstimatedTotal = externalEstimatedTotal.add(extPrice);

                Map<String, Object> missing = new LinkedHashMap<>();
                missing.put("role", roleDef.role());
                missing.put("roleName", roleDef.displayName());
                missing.put("quantity", roleDef.defaultQuantity());
                missing.put("necessity", roleDef.necessity());
                missing.put("suggestedItem", roleDef.externalEstimateDesc() != null ? roleDef.externalEstimateDesc() : roleDef.displayName());
                missing.put("estimatedPrice", extPrice);
                missing.put("formattedEstimatedPrice", formatCurrency(extPrice));
                missing.put("reason", "Cửa hàng hiện chưa kinh doanh mặt hàng chuyên dụng này, khách hàng cần chuẩn bị thêm bên ngoài.");
                missingItems.add(missing);
            }
        }

        BigDecimal grandTotal = shopTotal.add(externalEstimatedTotal);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projectType", projectType);
        data.put("items", items);
        data.put("shopItemCount", items.size());
        data.put("shopTotal", shopTotal);
        data.put("formattedShopTotal", formatCurrency(shopTotal));

        data.put("missingItems", missingItems);
        data.put("missingItemCount", missingItems.size());
        data.put("externalEstimatedTotal", externalEstimatedTotal);
        data.put("formattedExternalEstimatedTotal", formatCurrency(externalEstimatedTotal));

        data.put("grandTotal", grandTotal);
        data.put("formattedGrandTotal", formatCurrency(grandTotal));
        data.put("stockStatus", missingItems.isEmpty() ? "FULL_IN_STOCK" : "PARTIAL_IN_STOCK");

        return data;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", amount);
    }
}
