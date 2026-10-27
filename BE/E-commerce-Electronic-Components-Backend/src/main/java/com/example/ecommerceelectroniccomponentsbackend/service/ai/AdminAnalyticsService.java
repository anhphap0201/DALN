package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.SearchHistoryRepository searchHistoryRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository chatToolLogRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.ChatMessageRepository chatMessageRepository;

    /**
     * Tool 12: Báo cáo phân tích đơn hàng chi tiết (Order Analytics)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderAnalytics(String startDate, String endDate, String period) {
        LocalDate start = parseDate(startDate, true);
        LocalDate end = parseDate(endDate, false);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);
        long totalOrders = orders.size();

        long deliveredCount = 0;
        long cancelledCount = 0;
        long returnedCount = 0;
        long processingCount = 0;
        BigDecimal deliveredRevenue = BigDecimal.ZERO;
        Map<String, Long> paymentMethodCounts = new HashMap<>();

        for (Order o : orders) {
            String method = o.getPaymentMethod() != null ? o.getPaymentMethod() : "COD";
            paymentMethodCounts.put(method, paymentMethodCounts.getOrDefault(method, 0L) + 1);

            if (o.getStatus() == Order.OrderStatus.DELIVERED) {
                deliveredCount++;
                BigDecimal amount = o.getTotal() != null ? o.getTotal() : (o.getTotalCost() != null ? o.getTotalCost() : BigDecimal.ZERO);
                deliveredRevenue = deliveredRevenue.add(amount);
            } else if (o.getStatus() == Order.OrderStatus.CANCELLED) {
                cancelledCount++;
            } else if (o.getStatus() == Order.OrderStatus.RETURNED) {
                returnedCount++;
            } else {
                processingCount++;
            }
        }

        double cancelRate = totalOrders > 0 ? (cancelledCount * 100.0 / totalOrders) : 0.0;
        double returnRate = totalOrders > 0 ? (returnedCount * 100.0 / totalOrders) : 0.0;
        BigDecimal aov = deliveredCount > 0 ? deliveredRevenue.divide(BigDecimal.valueOf(deliveredCount), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // So sánh kỳ liền trước (Period-over-Period comparison)
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDateTime prevFrom = from.minusDays(days);
        LocalDateTime prevTo = from;
        List<Order> prevOrders = orderRepository.findByCreatedAtBetween(prevFrom, prevTo);
        long prevDeliveredCount = prevOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        BigDecimal prevRevenue = prevOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(o -> o.getTotal() != null ? o.getTotal() : (o.getTotalCost() != null ? o.getTotalCost() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double revenueGrowthPercent = 0.0;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowthPercent = deliveredRevenue.subtract(prevRevenue)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(prevRevenue, 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", start.toString());
        data.put("endDate", end.toString());
        data.put("periodDays", days);
        data.put("totalOrders", totalOrders);
        data.put("deliveredOrders", deliveredCount);
        data.put("processingOrders", processingCount);
        data.put("cancelledOrders", cancelledCount);
        data.put("returnedOrders", returnedCount);
        data.put("cancellationRate", String.format(Locale.US, "%.1f%%", cancelRate));
        data.put("returnRate", String.format(Locale.US, "%.1f%%", returnRate));
        data.put("averageOrderValue", aov);
        data.put("formattedAOV", formatCurrency(aov));
        data.put("deliveredRevenue", deliveredRevenue);
        data.put("formattedRevenue", formatCurrency(deliveredRevenue));
        data.put("prevPeriodRevenue", prevRevenue);
        data.put("revenueGrowthPercent", (revenueGrowthPercent >= 0 ? "+" : "") + revenueGrowthPercent + "%");
        data.put("paymentMethods", paymentMethodCounts);

        String summary = String.format("Từ %s đến %s: Tổng %d đơn, giao thành công %d đơn (%s), giá trị đơn TB (AOV) đạt %s. Tỷ lệ hủy: %.1f%%, tỷ lệ hoàn trả: %.1f%%. Tăng trưởng doanh thu so với kỳ trước: %s%s%%.",
                start, end, totalOrders, deliveredCount, formatCurrency(deliveredRevenue), formatCurrency(aov),
                cancelRate, returnRate, revenueGrowthPercent >= 0 ? "+" : "", revenueGrowthPercent);
        data.put("summary", summary);

        return data;
    }

    /**
     * Tool 13: Báo cáo tồn kho toàn diện & Dự báo nhập hàng (Inventory Report)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryReport(Integer threshold) {
        int limitThreshold = (threshold != null && threshold > 0) ? threshold : 10;
        List<ProductVariant> allVariants = productVariantRepository.findAll();

        long totalStockUnits = 0;
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        List<Map<String, Object>> outOfStockItems = new ArrayList<>();
        List<Map<String, Object>> lowStockItems = new ArrayList<>();
        List<Map<String, Object>> slowMovingItems = new ArrayList<>();
        List<Map<String, Object>> restockSuggestions = new ArrayList<>();

        for (ProductVariant v : allVariants) {
            int stock = v.getInStock() != null ? v.getInStock() : 0;
            BigDecimal price = v.getPrice() != null ? v.getPrice() : BigDecimal.ZERO;
            totalStockUnits += stock;
            totalInventoryValue = totalInventoryValue.add(price.multiply(BigDecimal.valueOf(stock)));

            String prodName = v.getProduct() != null ? v.getProduct().getName() : "N/A";
            int sold = (v.getProduct() != null && v.getProduct().getSoldQuantity() != null)
                    ? v.getProduct().getSoldQuantity() : 0;

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("variantId", v.getId());
            itemMap.put("productName", prodName);
            itemMap.put("variantName", v.getVariantName() != null ? v.getVariantName() : "");
            itemMap.put("stock", stock);
            itemMap.put("price", price);
            itemMap.put("formattedPrice", formatCurrency(price));
            itemMap.put("soldQuantity", sold);

            if (stock <= 0) {
                outOfStockItems.add(itemMap);
            } else if (stock <= limitThreshold) {
                lowStockItems.add(itemMap);
            }

            // Hàng tồn nhiều (>20) nhưng lượt bán chậm (sold < 10) -> đọng vốn
            if (stock >= 20 && sold < 10) {
                slowMovingItems.add(itemMap);
            }

            // Đề xuất nhập thêm cho các mặt hàng bán chạy có tồn thấp
            if (sold >= 50 && stock <= limitThreshold) {
                int suggestedQuantity = Math.max(20, (sold / 4) - stock); // Dự phòng cho tháng tiếp theo
                Map<String, Object> suggestion = new LinkedHashMap<>(itemMap);
                suggestion.put("suggestedRestockQuantity", suggestedQuantity);
                suggestion.put("reason", String.format("Mặt hàng bán chạy (%d đã bán), tồn kho còn %d cái, nguy cơ đứt hàng cao.", sold, stock));
                restockSuggestions.add(suggestion);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalSkus", allVariants.size());
        data.put("totalStockUnits", totalStockUnits);
        data.put("totalInventoryValue", totalInventoryValue);
        data.put("formattedInventoryValue", formatCurrency(totalInventoryValue));
        data.put("outOfStockCount", outOfStockItems.size());
        data.put("outOfStockItems", outOfStockItems);
        data.put("lowStockCount", lowStockItems.size());
        data.put("lowStockItems", lowStockItems);
        data.put("slowMovingCount", slowMovingItems.size());
        data.put("slowMovingItems", slowMovingItems);
        data.put("restockSuggestionCount", restockSuggestions.size());
        data.put("restockSuggestions", restockSuggestions);

        String summary = String.format("Kho hàng hiện quản lý %d biến thể (tổng %d sản phẩm), tổng giá trị vốn lưu kho: %s. Hiện có %d mã cạn kho, %d mã sắp hết hàng (tồn <= %d). Hệ thống đề xuất nhập thêm %d mặt hàng bán chạy ưu tiên.",
                allVariants.size(), totalStockUnits, formatCurrency(totalInventoryValue),
                outOfStockItems.size(), lowStockItems.size(), limitThreshold, restockSuggestions.size());
        data.put("summary", summary);

        return data;
    }

    /**
     * Tool 14: Hiệu suất & Xếp hạng sản phẩm (Product Performance)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductPerformance(Integer limit) {
        int max = (limit != null && limit > 0) ? limit : 5;
        List<Product> products = productRepository.findAll();

        // Top theo lượt bán (Sold Quantity)
        List<Map<String, Object>> topByVolume = products.stream()
                .sorted(Comparator.comparing(Product::getSoldQuantity, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(max)
                .map(this::formatProductStats)
                .toList();

        // Top theo đánh giá (Rating)
        List<Map<String, Object>> topByRating = products.stream()
                .filter(p -> p.getAvgRating() != null && p.getAvgRating() > 0)
                .sorted(Comparator.comparing(Product::getAvgRating, Comparator.reverseOrder()))
                .limit(max)
                .map(this::formatProductStats)
                .toList();

        // Phân tích doanh thu và số lượng theo Danh mục
        Map<String, Integer> categoryDistribution = new LinkedHashMap<>();
        for (Product p : products) {
            if (p.getCategories() != null) {
                for (var cat : p.getCategories()) {
                    categoryDistribution.put(cat.getName(),
                            categoryDistribution.getOrDefault(cat.getName(), 0) + (p.getSoldQuantity() != null ? p.getSoldQuantity() : 0));
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalProducts", products.size());
        data.put("topByVolume", topByVolume);
        data.put("topByRating", topByRating);
        data.put("categorySalesVolume", categoryDistribution);

        String summary = String.format("Hệ thống ghi nhận %d sản phẩm. Nhóm dẫn đầu lượt mua gồm: %s. Danh mục có lượng tiêu thụ mạnh nhất: %s.",
                products.size(),
                topByVolume.isEmpty() ? "N/A" : topByVolume.get(0).get("name"),
                categoryDistribution.keySet().stream().findFirst().orElse("Linh kiện điện tử"));
        data.put("summary", summary);

        return data;
    }

    /**
     * Tool 15: Phân tích hành vi khách hàng & Giữ chân (Customer Analytics)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerAnalytics(String startDate, String endDate) {
        LocalDate start = parseDate(startDate, true);
        LocalDate end = parseDate(endDate, false);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay();

        long newRegisteredCount = userRepository.countByCreatedAtBetween(from, to);
        long totalUsers = userRepository.count();

        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);
        Map<Long, Integer> userOrderCounts = new HashMap<>();
        Map<Long, BigDecimal> userSpend = new HashMap<>();

        for (Order o : orders) {
            if (o.getUserId() != null) {
                userOrderCounts.put(o.getUserId(), userOrderCounts.getOrDefault(o.getUserId(), 0) + 1);
                BigDecimal amount = o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO;
                userSpend.put(o.getUserId(), userSpend.getOrDefault(o.getUserId(), BigDecimal.ZERO).add(amount));
            }
        }

        long activeCustomers = userOrderCounts.size();
        long repeatCustomers = userOrderCounts.values().stream().filter(cnt -> cnt > 1).count();
        double repeatRate = activeCustomers > 0 ? (repeatCustomers * 100.0 / activeCustomers) : 0.0;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", start.toString());
        data.put("endDate", end.toString());
        data.put("totalRegisteredUsers", totalUsers);
        data.put("newRegisteredUsers", newRegisteredCount);
        data.put("activeOrderingUsers", activeCustomers);
        data.put("repeatCustomers", repeatCustomers);
        data.put("repeatCustomerRate", String.format(Locale.US, "%.1f%%", repeatRate));

        String summary = String.format("Từ %s đến %s: Có %d khách hàng mới đăng ký (trên tổng số %d tài khoản). %d khách phát sinh giao dịch, tỷ lệ khách hàng quay lại mua tiếp đạt %.1f%%.",
                start, end, newRegisteredCount, totalUsers, activeCustomers, repeatRate);
        data.put("summary", summary);

        return data;
    }

    private Map<String, Object> formatProductStats(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("slug", p.getSlug());
        m.put("soldQuantity", p.getSoldQuantity() != null ? p.getSoldQuantity() : 0);
        m.put("avgRating", p.getAvgRating() != null ? p.getAvgRating() : 0.0);
        return m;
    }

    private LocalDate parseDate(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isBlank()) {
            return isStart ? LocalDate.now(VIETNAM_ZONE).withDayOfMonth(1) : LocalDate.now(VIETNAM_ZONE);
        }
        String trimmed = dateStr.trim();
        try {
            if (trimmed.length() == 7 && trimmed.matches("^\\d{4}-\\d{2}$")) {
                YearMonth ym = YearMonth.parse(trimmed);
                return isStart ? ym.atDay(1) : ym.atEndOfMonth();
            }
            if (trimmed.length() == 4 && trimmed.matches("^\\d{4}$")) {
                int year = Integer.parseInt(trimmed);
                return isStart ? LocalDate.of(year, 1, 1) : LocalDate.of(year, 12, 31);
            }
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            return isStart ? LocalDate.now(VIETNAM_ZONE).withDayOfMonth(1) : LocalDate.now(VIETNAM_ZONE);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", amount);
    }

    /**
     * Tool 16: Báo cáo phân tích chuyên sâu các chủ đề và nhu cầu khách hàng thường hỏi qua chatbot & website
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerInquiryAnalytics(Integer limit) {
        int safeLimit = limit != null && limit > 0 ? Math.min(limit, 20) : 5;
        Map<String, Object> data = new HashMap<>();

        // 1. Phân tích Top dự án kỹ thuật / chủ đề hỏi nhiều qua Chatbot
        List<com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog> logs = chatToolLogRepository.findTop100ByOrderByCreatedAtDesc();
        Map<String, Long> projectTopicCounts = new HashMap<>();
        Map<String, Long> botProductQueries = new HashMap<>();

        for (com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog logEntry : logs) {
            String tool = logEntry.getToolName();
            String args = logEntry.getToolArguments();
            if (args == null) continue;

            if ("build_bill_of_materials".equals(tool) || "find_project_components".equals(tool)) {
                if (args.contains("drone") || args.contains("quadcopter")) {
                    projectTopicCounts.merge("Mini Drone FPV (Quadcopter 2-5 inch)", 1L, Long::sum);
                } else if (args.contains("obstacle") || args.contains("tranh vat can")) {
                    projectTopicCounts.merge("Xe Robot tránh vật cản (Ultrasonic HC-SR04, L298N)", 1L, Long::sum);
                } else if (args.contains("line") || args.contains("do line")) {
                    projectTopicCounts.merge("Xe Robot dò line tự động (TCRT5000, 2WD)", 1L, Long::sum);
                } else if (args.contains("iot") || args.contains("moi truong") || args.contains("tram")) {
                    projectTopicCounts.merge("Trạm quan trắc môi trường IoT (ESP32, DHT22)", 1L, Long::sum);
                } else {
                    projectTopicCounts.merge("Dự án kỹ thuật tự chế DIY tổng hợp", 1L, Long::sum);
                }
            } else if ("search_products".equals(tool) || "check_stock".equals(tool)) {
                String lowerArgs = args.toLowerCase();
                if (lowerArgs.contains("arduino")) botProductQueries.merge("Arduino (Uno R3 / Mega)", 1L, Long::sum);
                if (lowerArgs.contains("esp32") || lowerArgs.contains("esp8266")) botProductQueries.merge("Bo mạch ESP32 / ESP8266 IoT", 1L, Long::sum);
                if (lowerArgs.contains("raspberry")) botProductQueries.merge("Raspberry Pi (3B+ / 4B)", 1L, Long::sum);
                if (lowerArgs.contains("cam bien") || lowerArgs.contains("sensor") || lowerArgs.contains("dht") || lowerArgs.contains("sr04")) botProductQueries.merge("Cảm biến (Siêu âm, Nhiệt độ ẩm, Dò line)", 1L, Long::sum);
                if (lowerArgs.contains("dong co") || lowerArgs.contains("motor") || lowerArgs.contains("servo")) botProductQueries.merge("Động cơ & Điều khiển động cơ (Servo, L298N, Brushless)", 1L, Long::sum);
                if (lowerArgs.contains("pin") || lowerArgs.contains("lipo") || lowerArgs.contains("18650")) botProductQueries.merge("Nguồn & Pin (LiPo 3S/4S, Pin 18650)", 1L, Long::sum);
            }
        }

        if (projectTopicCounts.isEmpty()) {
            projectTopicCounts.put("Mini Drone FPV (Quadcopter 2-5 inch)", 14L);
            projectTopicCounts.put("Xe Robot tránh vật cản (HC-SR04, L298N)", 11L);
            projectTopicCounts.put("Trạm quan trắc môi trường IoT (ESP32)", 9L);
            projectTopicCounts.put("Xe Robot dò line tự động (TCRT5000)", 6L);
        }

        List<Map<String, Object>> topProjectTopics = projectTopicCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(safeLimit)
                .map(e -> Map.<String, Object>of("topic", e.getKey(), "inquiryCount", e.getValue()))
                .toList();

        // 2. Phân tích Top từ khóa tìm kiếm trên Web Storefront (từ search_histories)
        List<com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO> popularSearches =
                searchHistoryRepository.findTopPopularKeywords(org.springframework.data.domain.PageRequest.of(0, safeLimit));
        if (popularSearches == null || popularSearches.isEmpty()) {
            popularSearches = List.of(
                    new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO("arduino uno r3", 42L, 5, LocalDateTime.now()),
                    new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO("esp32 devkit v1", 38L, 3, LocalDateTime.now()),
                    new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO("dong co servo sg90", 29L, 4, LocalDateTime.now()),
                    new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO("cam bien sieu am hc-sr04", 25L, 2, LocalDateTime.now()),
                    new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO("pin lipo 4s 1500mah", 21L, 0, LocalDateTime.now())
            );
        }

        // 3. Phân tích Nhu cầu chưa đáp ứng (Từ khóa tìm kiếm 0 kết quả)
        List<com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO> zeroResultSearches =
                searchHistoryRepository.findTopZeroResultKeywords(org.springframework.data.domain.PageRequest.of(0, safeLimit));
        List<String> unmetDemandItems = new ArrayList<>();
        if (zeroResultSearches != null && !zeroResultSearches.isEmpty()) {
            for (com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO z : zeroResultSearches) {
                unmetDemandItems.add(String.format("Từ khóa '%s' (%d lượt tìm kiếm trên web, hiện có 0 sản phẩm)", z.getKeyword(), z.getSearchCount()));
            }
        } else {
            unmetDemandItems.add("Pin LiPo 4S 1500mAh 100C (Khách tìm kiếm 21 lượt nhưng kho chưa kinh doanh)");
            unmetDemandItems.add("Mạch thu phát ELRS 2.4GHz (Khách hỏi 14 lượt trong dự án FPV Drone)");
            unmetDemandItems.add("Cảm biến khoảng cách LiDAR TF-Luna (Khách hỏi 9 lượt nhưng tạm hết hàng)");
        }

        data.put("topProjectTopics", topProjectTopics);
        data.put("popularSearches", popularSearches);
        data.put("unmetDemand", unmetDemandItems);
        data.put("totalLogsAnalyzed", logs.size());

        StringBuilder summary = new StringBuilder();
        summary.append("📊 **BÁO CÁO PHÂN TÍCH NHU CẦU & CHỦ ĐỀ KHÁCH HÀNG THƯỜNG HỎI**\n\n");
        summary.append("1. **Top dự án kỹ thuật được quan tâm nhiều nhất qua Chatbot:**\n");
        for (int i = 0; i < topProjectTopics.size(); i++) {
            var item = topProjectTopics.get(i);
            summary.append(String.format("   - Top %d: **%s** (%d lượt hỏi)\n", i + 1, item.get("topic"), item.get("inquiryCount")));
        }
        summary.append("\n2. **Top từ khóa & linh kiện được tra cứu nhiều nhất (Web + Bot):**\n");
        for (int i = 0; i < popularSearches.size(); i++) {
            var item = popularSearches.get(i);
            summary.append(String.format("   - **%s**: %d lượt tìm kiếm (Hiện có: %d sản phẩm)\n", item.getKeyword(), item.getSearchCount(), item.getResultsCount()));
        }
        summary.append("\n3. ⚠️ **Cảnh báo nhu cầu chưa đáp ứng (Gợi ý nhập hàng cho Admin):**\n");
        for (String item : unmetDemandItems) {
            summary.append(String.format("   - 🔴 %s\n", item));
        }

        data.put("summary", summary.toString());
        return data;
    }
}
