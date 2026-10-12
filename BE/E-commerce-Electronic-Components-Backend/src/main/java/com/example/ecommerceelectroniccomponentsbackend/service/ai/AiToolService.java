package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiToolService {

    private final IProductSearchService productSearchService;
    private final ProductVariantRepository productVariantRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ChatToolLogRepository chatToolLogRepository;
    private final ObjectMapper objectMapper;



    /**
     * Tool 1: Tìm kiếm sản phẩm theo tên hoặc từ khóa kỹ thuật
     */
    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(String query, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            List<ProductWithVariantsDTO> products = productSearchService.searchProductsByName(query);
            result.put("query", query);
            result.put("totalFound", products.size());
            result.put("products", products);
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "search_products", Map.of("query", query), result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 2: Kiểm tra tình trạng tồn kho của sản phẩm
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkStock(Long productId, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            ProductWithVariantsDTO product = productSearchService.getProductById(productId);
            if (product == null) {
                result.put("error", "Không tìm thấy sản phẩm có ID: " + productId);
                return result;
            }

            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            List<Map<String, Object>> variantStockList = new ArrayList<>();
            int totalStock = 0;

            for (ProductVariant v : variants) {
                Map<String, Object> vMap = new HashMap<>();
                vMap.put("variantId", v.getId());
                vMap.put("variantName", v.getVariantName());
                vMap.put("price", v.getPrice());
                vMap.put("discountPrice", v.getDiscountPrice());
                vMap.put("inStock", v.getInStock());
                vMap.put("isAvailable", v.getIsAvailable());
                variantStockList.add(vMap);
                if (v.getInStock() != null) {
                    totalStock += v.getInStock();
                }
            }

            result.put("productId", productId);
            result.put("productName", product.getName());
            result.put("totalStock", totalStock);
            result.put("variants", variantStockList);
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi khi tra cứu tồn kho: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "check_stock", Map.of("productId", productId), result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 3: Tra cứu các sản phẩm sắp hết hàng (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLowStockProducts(Integer threshold, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            // Kiểm tra phân quyền RBAC
            if (userRole == null || !userRole.contains("ADMIN")) {
                result.put("error", "Từ chối truy cập: Chỉ Quản trị viên (Admin) mới có quyền truy vấn số liệu tồn kho cảnh báo.");
                return result;
            }

            int limitThreshold = (threshold != null && threshold > 0) ? threshold : 10;
            List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(limitThreshold);

            List<Map<String, Object>> items = new ArrayList<>();
            for (ProductVariant v : lowStockVariants) {
                Map<String, Object> item = new HashMap<>();
                item.put("variantId", v.getId());
                item.put("productName", v.getProduct() != null ? v.getProduct().getName() : "N/A");
                item.put("variantName", v.getVariantName());
                item.put("inStock", v.getInStock());
                item.put("price", v.getPrice());
                items.add(item);
            }

            result.put("threshold", limitThreshold);
            result.put("count", items.size());
            result.put("lowStockItems", items);
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi tra cứu cảnh báo tồn kho: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "get_low_stock_products", Map.of("threshold", threshold != null ? threshold : 10), result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 4: Tra cứu top sản phẩm bán chạy nhất theo số lượng đã bán
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTopSellingProducts(Integer limit, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            List<com.example.ecommerceelectroniccomponentsbackend.entity.Product> topProducts = productRepository.findTop10ByOrderBySoldQuantityDesc();
            int maxCount = (limit != null && limit > 0) ? Math.min(limit, topProducts.size()) : Math.min(5, topProducts.size());

            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < maxCount; i++) {
                var p = topProducts.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("name", p.getName());
                item.put("slug", p.getSlug());
                item.put("soldQuantity", p.getSoldQuantity());
                item.put("avgRating", p.getAvgRating());
                items.add(item);
            }

            result.put("count", items.size());
            result.put("topSellingProducts", items);
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi tra cứu mặt hàng bán chạy: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "get_top_selling_products", Map.of("limit", limit != null ? limit : 5), result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 5: Báo cáo doanh thu theo khoảng thời gian (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueReport(String startDate, String endDate, String period, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            if (userRole == null || !userRole.contains("ADMIN")) {
                result.put("error", "Từ chối truy cập: Chỉ Quản trị viên (Admin) mới có quyền truy vấn số liệu doanh thu tài chính.");
                return result;
            }

            java.time.LocalDate start = parseDateFlexible(startDate, true);
            java.time.LocalDate end = parseDateFlexible(endDate, false);

            if (start.isAfter(end)) {
                java.time.LocalDate tmp = start;
                start = end;
                end = tmp;
            }

            java.time.LocalDateTime from = start.atStartOfDay();
            java.time.LocalDateTime to = end.plusDays(1).atStartOfDay();

            java.math.BigDecimal revenue = orderRepository.sumRevenueByPeriod(from, to);
            if (revenue == null) revenue = java.math.BigDecimal.ZERO;
            long deliveredCount = orderRepository.countDeliveredByPeriod(from, to);
            long totalOrders = orderRepository.countByCreatedAtBetween(from, to);

            String formattedRevenue = String.format("%,.0f VNĐ", revenue.doubleValue());

            result.put("startDate", start.toString());
            result.put("endDate", end.toString());
            result.put("period", period != null ? period : "range");
            result.put("totalRevenue", revenue);
            result.put("formattedRevenue", formattedRevenue);
            result.put("deliveredOrders", deliveredCount);
            result.put("totalOrders", totalOrders);
            result.put("summary", String.format("Doanh thu từ %s đến %s đạt %s với %d đơn hoàn thành (trên tổng số %d đơn đặt).",
                    start, end, formattedRevenue, deliveredCount, totalOrders));
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi khi thống kê doanh thu: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "get_revenue_report",
                    Map.of("startDate", startDate != null ? startDate : "", "endDate", endDate != null ? endDate : "", "period", period != null ? period : ""),
                    result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 6: Tra cứu trạng thái đơn hàng (Khách hàng & Admin)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> trackOrder(String orderCode, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> result = new HashMap<>();

        try {
            if (orderCode == null || orderCode.isBlank()) {
                result.put("error", "Vui lòng cung cấp mã đơn hàng để tra cứu.");
                return result;
            }

            String cleanCode = orderCode.trim().replaceAll("^#+", "").trim();
            Optional<com.example.ecommerceelectroniccomponentsbackend.entity.Order> opt = orderRepository.findByOrderNumberWithItems(cleanCode);
            if (opt.isEmpty()) {
                opt = orderRepository.findByOrderNumberIgnoreCase(cleanCode);
            }
            if (opt.isEmpty()) {
                opt = orderRepository.findByLockerOrderId(cleanCode);
            }

            if (opt.isEmpty()) {
                result.put("error", "Không tìm thấy đơn hàng với mã \"" + orderCode + "\". Vui lòng kiểm tra lại mã đơn hàng.");
                return result;
            }

            com.example.ecommerceelectroniccomponentsbackend.entity.Order order = opt.get();
            String statusDesc = getStatusDescription(order.getStatus());
            String formattedTotal = order.getTotal() != null ? String.format("%,.0f VNĐ", order.getTotal().doubleValue()) : "0 VNĐ";

            result.put("orderNumber", order.getOrderNumber());
            result.put("status", order.getStatus() != null ? order.getStatus().name() : "PENDING");
            result.put("statusDescription", statusDesc);
            result.put("totalAmount", order.getTotal());
            result.put("formattedTotal", formattedTotal);
            result.put("customerName", order.getFullName());
            result.put("maskedPhone", maskPhone(order.getPhone()));
            result.put("paymentMethod", order.getPaymentMethod());
            result.put("paymentStatus", order.getPaymentStatus());
            result.put("shippingMethod", order.getShippingMethod());
            result.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");

            List<Map<String, Object>> items = new ArrayList<>();
            if (order.getOrderItems() != null) {
                for (com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem item : order.getOrderItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    String prodName = item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                            ? item.getProductVariant().getProduct().getName() : "Linh kiện";
                    String variantName = item.getProductVariant() != null ? item.getProductVariant().getVariantName() : "";
                    itemMap.put("productName", prodName + (variantName != null && !variantName.isBlank() ? " (" + variantName + ")" : ""));
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    items.add(itemMap);
                }
            }
            result.put("items", items);
            result.put("totalItems", items.size());
            result.put("message", String.format("Đơn hàng %s đang ở trạng thái: '%s'. Tổng giá trị: %s. Hình thức: %s (%s).",
                    order.getOrderNumber(), statusDesc, formattedTotal, order.getPaymentMethod(), order.getPaymentStatus()));
            return result;
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            result.put("error", "Lỗi khi tra cứu đơn hàng: " + e.getMessage());
            return result;
        } finally {
            logToolExecution(sessionId, userId, "track_order", Map.of("orderCode", orderCode != null ? orderCode : ""),
                    result, status, System.currentTimeMillis() - startTime, error);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    private String getStatusDescription(com.example.ecommerceelectroniccomponentsbackend.entity.Order.OrderStatus status) {
        if (status == null) return "Chờ xử lý";
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận / Đang chuẩn bị hàng";
            case SHIPPED -> "Đang giao hàng";
            case DELIVERED -> "Đã giao thành công";
            case CANCELLED -> "Đã hủy đơn";
        };
    }

    private java.time.LocalDate parseDateFlexible(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isBlank()) {
            return isStart ? java.time.LocalDate.now().withDayOfMonth(1) : java.time.LocalDate.now();
        }
        String trimmed = dateStr.trim();
        try {
            if (trimmed.length() == 7 && trimmed.matches("^\\d{4}-\\d{2}$")) {
                java.time.YearMonth ym = java.time.YearMonth.parse(trimmed);
                return isStart ? ym.atDay(1) : ym.atEndOfMonth();
            }
            if (trimmed.length() == 4 && trimmed.matches("^\\d{4}$")) {
                int year = Integer.parseInt(trimmed);
                return isStart ? java.time.LocalDate.of(year, 1, 1) : java.time.LocalDate.of(year, 12, 31);
            }
            return java.time.LocalDate.parse(trimmed);
        } catch (Exception e) {
            return isStart ? java.time.LocalDate.now().withDayOfMonth(1) : java.time.LocalDate.now();
        }
    }

    /**
     * Trả về danh sách đặc tả công cụ (Tool Definitions) theo chuẩn OpenAI / Ollama
     */
    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // Tool 1: search_products
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "search_products",
                        "description", "Tìm kiếm linh kiện điện tử, vi điều khiển, cảm biến hoặc module theo tên hoặc từ khóa kỹ thuật.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "Từ khóa hoặc tên linh kiện cần tìm kiếm (vd: Arduino Uno, ESP32, DHT22, điện trở,...)"
                                        )
                                ),
                                "required", List.of("query")
                        )
                )
        ));

        // Tool 2: check_stock
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "check_stock",
                        "description", "Kiểm tra số lượng tồn kho thực tế và các biến thể của sản phẩm theo ID.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "productId", Map.of(
                                                "type", "integer",
                                                "description", "ID số nguyên của sản phẩm cần kiểm tra tồn kho"
                                        )
                                ),
                                "required", List.of("productId")
                        )
                )
        ));

        // Tool 3: get_low_stock_products
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_low_stock_products",
                        "description", "Tra cứu danh sách các linh kiện có số lượng tồn kho thấp hơn hoặc bằng ngưỡng quy định để cảnh báo nhập hàng.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "threshold", Map.of(
                                                "type", "integer",
                                                "description", "Ngưỡng số lượng tồn kho tối thiểu (mặc định là 10 nếu không chỉ định)"
                                        )
                                )
                        )
                )
        ));

        // Tool 4: get_top_selling_products
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_top_selling_products",
                        "description", "Tra cứu danh sách các mặt hàng, linh kiện điện tử bán chạy nhất của cửa hàng dựa trên số lượng đã bán (sold quantity).",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "limit", Map.of(
                                                "type", "integer",
                                                "description", "Số lượng mặt hàng bán chạy cần lấy (mặc định 5 hoặc 10)"
                                        )
                                )
                        )
                )
        ));

        // Tool 5: get_revenue_report
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_revenue_report",
                        "description", "Truy vấn báo cáo tổng doanh thu và số lượng đơn hàng theo ngày, tháng hoặc khoảng thời gian. Yêu cầu quyền Quản trị viên (Admin).",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "start_date", Map.of(
                                                "type", "string",
                                                "description", "Ngày bắt đầu theo định dạng YYYY-MM-DD (vd: 2026-10-01)"
                                        ),
                                        "end_date", Map.of(
                                                "type", "string",
                                                "description", "Ngày kết thúc theo định dạng YYYY-MM-DD (vd: 2026-10-31)"
                                        ),
                                        "period", Map.of(
                                                "type", "string",
                                                "enum", List.of("day", "month", "year", "range"),
                                                "description", "Loại thống kê: day, month, year hoặc range"
                                        )
                                ),
                                "required", List.of("start_date", "end_date")
                        )
                )
        ));

        // Tool 6: track_order
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "track_order",
                        "description", "Kiểm tra tình trạng vận chuyển, trạng thái xử lý, tổng tiền và phương thức thanh toán của đơn hàng theo mã đơn hàng.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "order_code", Map.of(
                                                "type", "string",
                                                "description", "Mã đơn hàng khách hàng cung cấp (vd: ORD-12345, #ORD-12345,...)"
                                        )
                                ),
                                "required", List.of("order_code")
                        )
                )
        ));

        return tools;
    }


    private void logToolExecution(UUID sessionId, Long userId, String toolName, Object args, Object result, String status, long durationMs, String errorCode) {
        try {
            String argsJson = objectMapper.writeValueAsString(args);
            String resultJson = objectMapper.writeValueAsString(result);
            ChatToolLog logEntry = ChatToolLog.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .toolName(toolName)
                    .toolArguments(argsJson)
                    .toolResult(resultJson)
                    .status(status)
                    .durationMs(durationMs)
                    .errorCode(errorCode)
                    .build();
            chatToolLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Không thể ghi log chat tool: {}", e.getMessage());
        }
    }
}
