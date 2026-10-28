package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
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

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiToolService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final IProductSearchService productSearchService;
    private final ProductVariantRepository productVariantRepository;
    private final com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ChatToolLogRepository chatToolLogRepository;
    private final ObjectMapper objectMapper;
    private final ProjectPlanningService projectPlanningService;
    private final AdminAnalyticsService adminAnalyticsService;
    private final AiSecuritySanitizer aiSecuritySanitizer;
    private final AiToolPolicyService aiToolPolicyService;
    private final com.example.ecommerceelectroniccomponentsbackend.service.IWarrantyService warrantyService;

    /**
     * Helper chuẩn hóa kết quả thành công cho mọi Tool
     */
    private Map<String, Object> buildSuccessResult(String message, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        if (data != null) {
            result.putAll(data);
        }
        result.put("success", true);
        result.put("message", message);
        result.put("error", null);
        result.put("data", data);
        return result;
    }

    /**
     * Helper chuẩn hóa kết quả lỗi cho mọi Tool
     */
    private Map<String, Object> buildErrorResult(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", null);
        result.put("error", errorMessage);
        result.put("data", null);
        return result;
    }

    /**
     * Tool 1: Tìm kiếm sản phẩm theo tên hoặc từ khóa kỹ thuật
     */
    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(String query, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            List<ProductWithVariantsDTO> products = productSearchService.searchProductsByName(query);
            data.put("query", query);
            data.put("totalFound", products.size());
            data.put("products", products);

            String msg = products.isEmpty()
                    ? "Không tìm thấy linh kiện nào với từ khóa: " + query
                    : "Tìm thấy " + products.size() + " linh kiện phù hợp.";
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "search_products", Map.of("query", query != null ? query : ""), data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 2: Gợi ý sản phẩm theo nhu cầu và ngân sách
     */
    @Transactional(readOnly = true)
    public Map<String, Object> recommendProducts(String request, BigDecimal maxBudget, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            List<String> keywords = recommendationKeywords(request);
            List<ProductWithVariantsDTO> products = productRepository.findAll().stream()
                    .map(productSearchService::convertToProductWithVariantsDTO)
                    .filter(product -> product.getTotalStock() != null && product.getTotalStock() > 0)
                    .filter(product -> effectivePrice(product) != null)
                    .filter(product -> maxBudget == null || effectivePrice(product).compareTo(maxBudget) <= 0)
                    .map(product -> Map.entry(product, recommendationScore(product, keywords)))
                    .filter(entry -> keywords.isEmpty() || entry.getValue() > 0)
                    .sorted(Comparator.<Map.Entry<ProductWithVariantsDTO, Integer>>comparingInt(Map.Entry::getValue)
                            .reversed()
                            .thenComparing(entry -> effectivePrice(entry.getKey())))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            data.put("query", request);
            data.put("maxBudget", maxBudget);
            data.put("totalFound", products.size());
            data.put("products", products);

            String msg = products.isEmpty()
                    ? "Không tìm thấy linh kiện phù hợp trong kho với ngân sách/nhu cầu: " + request
                    : "Đã tìm thấy " + products.size() + " sản phẩm phù hợp nhất trong kho.";
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi gợi ý sản phẩm: " + e.getMessage());
        } finally {
            Map<String, Object> params = new HashMap<>();
            params.put("query", request != null ? request : "");
            params.put("maxBudget", maxBudget);
            logToolExecution(sessionId, userId, "recommend_products", params, data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 3: So sánh thông số kỹ thuật, giá cả, và tồn kho giữa các sản phẩm
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compareProducts(List<String> productNames, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            if (productNames == null || productNames.isEmpty()) {
                return buildErrorResult("Vui lòng cung cấp ít nhất hai tên sản phẩm để thực hiện so sánh.");
            }

            List<Map<String, Object>> productDetails = new ArrayList<>();
            for (String rawName : productNames) {
                String query = normalizeProductQuery(rawName);
                if (query.isBlank()) continue;
                List<ProductWithVariantsDTO> matches = productSearchService.searchProductsByName(query);
                if (!matches.isEmpty()) {
                    ProductWithVariantsDTO p = matches.get(0);
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("id", p.getId());
                    pMap.put("name", p.getName());
                    pMap.put("slug", p.getSlug());
                    BigDecimal price = p.getMinDiscountPrice() != null ? p.getMinDiscountPrice() : p.getMinPrice();
                    pMap.put("price", price);
                    pMap.put("formattedPrice", formatCurrency(price));
                    pMap.put("totalStock", p.getTotalStock() != null ? p.getTotalStock() : 0);
                    pMap.put("avgRating", p.getAvgRating() != null ? p.getAvgRating() : 0.0);
                    pMap.put("soldQuantity", p.getSoldQuantity() != null ? p.getSoldQuantity() : 0);

                    List<String> catNames = p.getCategories() != null
                            ? p.getCategories().stream().map(com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO::getName).toList()
                            : List.of();
                    pMap.put("categories", catNames);

                    List<Map<String, Object>> varSpecs = new ArrayList<>();
                    if (p.getVariants() != null) {
                        for (var v : p.getVariants()) {
                            Map<String, Object> vMap = new HashMap<>();
                            vMap.put("variantName", v.getVariantName());
                            vMap.put("price", v.getPrice());
                            vMap.put("inStock", v.getInStock());
                            vMap.put("specifications", v.getSpecifications());
                            vMap.put("description", v.getDescription());
                            varSpecs.add(vMap);
                        }
                    }
                    pMap.put("variants", varSpecs);
                    productDetails.add(pMap);
                }
            }

            if (productDetails.size() < 2) {
                return buildErrorResult("Hệ thống chỉ tìm thấy " + productDetails.size() + " sản phẩm tương ứng trong cơ sở dữ liệu. Vui lòng cung cấp tên đầy đủ của ít nhất 2 sản phẩm.");
            }

            data.put("comparedProducts", productDetails);
            data.put("totalCompared", productDetails.size());

            StringBuilder summary = new StringBuilder("So sánh thông số & giá:\n");
            for (var p : productDetails) {
                summary.append(String.format("- %s: Giá %s, Tồn kho: %d, Đánh giá: %.1f/5\n",
                        p.get("name"), p.get("formattedPrice"), p.get("totalStock"), ((Number) p.get("avgRating")).doubleValue()));
            }
            data.put("summary", summary.toString().trim());

            return buildSuccessResult("Đã so sánh chi tiết " + productDetails.size() + " sản phẩm.", data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi so sánh sản phẩm: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "compare_products", Map.of("productNames", productNames != null ? productNames : List.of()), data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 4: Kiểm tra tình trạng tồn kho của sản phẩm theo ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkStock(Long productId, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            ProductWithVariantsDTO product = productSearchService.getProductById(productId);
            if (product == null) {
                return buildErrorResult("Không tìm thấy sản phẩm có ID: " + productId);
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

            data.put("productId", productId);
            data.put("productName", product.getName());
            data.put("totalStock", totalStock);
            data.put("variants", variantStockList);

            String msg = String.format("Sản phẩm '%s' hiện có tổng cộng %d linh kiện trong kho qua %d biến thể.",
                    product.getName(), totalStock, variantStockList.size());
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi tra cứu tồn kho: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "check_stock", Map.of("productId", productId != null ? productId : 0), data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 5: Tra cứu các sản phẩm sắp hết hàng (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLowStockProducts(Integer threshold, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_low_stock_products", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
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

            data.put("threshold", limitThreshold);
            data.put("count", items.size());
            data.put("lowStockItems", items);

            String msg = String.format("Tìm thấy %d biến thể có tồn kho <= %d để chuẩn bị nhập hàng.", items.size(), limitThreshold);
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi tra cứu cảnh báo tồn kho: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_low_stock_products", Map.of("threshold", threshold != null ? threshold : 10), data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 6: Tra cứu top sản phẩm bán chạy nhất
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTopSellingProducts(Integer limit, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

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
                ProductWithVariantsDTO details = productSearchService.getProductById(p.getId());
                if (details != null) {
                    item.put("defaultImageUrl", details.getDefaultImageUrl());
                    item.put("minPrice", details.getMinPrice());
                    item.put("minDiscountPrice", details.getMinDiscountPrice());
                    item.put("totalStock", details.getTotalStock());
                }
                items.add(item);
            }

            data.put("count", items.size());
            data.put("topSellingProducts", items);

            String msg = String.format("Lấy thành công danh sách %d mặt hàng bán chạy nhất.", items.size());
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi tra cứu mặt hàng bán chạy: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_top_selling_products", Map.of("limit", limit != null ? limit : 5), data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 7: Báo cáo doanh thu theo khoảng thời gian (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueReport(String startDate, String endDate, String period, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_revenue_report", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }

            LocalDate start = parseDateFlexible(startDate, true);
            LocalDate end = parseDateFlexible(endDate, false);

            if (start.isAfter(end)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }

            LocalDateTime from = start.atStartOfDay();
            LocalDateTime to = end.plusDays(1).atStartOfDay();

            BigDecimal revenue = orderRepository.sumRevenueByPeriod(from, to);
            if (revenue == null) revenue = BigDecimal.ZERO;
            long deliveredCount = orderRepository.countDeliveredByPeriod(from, to);
            long totalOrders = orderRepository.countByCreatedAtBetween(from, to);

            String formattedRevenue = String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", revenue.doubleValue());

            data.put("startDate", start.toString());
            data.put("endDate", end.toString());
            data.put("period", period != null ? period : "range");
            data.put("totalRevenue", revenue);
            data.put("formattedRevenue", formattedRevenue);
            data.put("deliveredOrders", deliveredCount);
            data.put("totalOrders", totalOrders);
            String summary = String.format("Doanh thu từ %s đến %s đạt %s với %d đơn hoàn thành (trên tổng số %d đơn đặt).",
                    start, end, formattedRevenue, deliveredCount, totalOrders);
            data.put("summary", summary);

            return buildSuccessResult(summary, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi thống kê doanh thu: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_revenue_report",
                    Map.of("startDate", startDate != null ? startDate : "", "endDate", endDate != null ? endDate : "", "period", period != null ? period : ""),
                    data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 8: Tra cứu trạng thái đơn hàng (Có kiểm soát quyền sở hữu & che thông tin nhạy cảm)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> trackOrder(String orderCode, String phoneNumber, String userRole, Long currentUserId, UUID sessionId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            if (orderCode == null || orderCode.isBlank()) {
                return buildErrorResult("Vui lòng cung cấp mã đơn hàng để tra cứu.");
            }

            String cleanCode = orderCode.trim().replaceAll("^#+", "").trim();
            Optional<Order> opt = orderRepository.findByOrderNumberWithItems(cleanCode);
            if (opt.isEmpty()) {
                opt = orderRepository.findByOrderNumberIgnoreCase(cleanCode);
            }
            if (opt.isEmpty()) {
                opt = orderRepository.findByLockerOrderId(cleanCode);
            }

            if (opt.isEmpty()) {
                return buildErrorResult("Không tìm thấy đơn hàng với mã \"" + orderCode + "\". Vui lòng kiểm tra lại mã đơn hàng.");
            }

            Order order = opt.get();

            // Kiểm soát quyền hạn (RBAC & Ownership Security qua Policy Service)
            var orderDecision = aiToolPolicyService.checkOrderAccess(order, currentUserId, userRole, phoneNumber);
            if (!orderDecision.isAllowed()) {
                return buildErrorResult(orderDecision.getReason());
            }

            boolean isFullDetails = orderDecision.isFullDetails();

            String statusDesc = getStatusDescription(order.getStatus());
            String formattedTotal = order.getTotal() != null
                    ? String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", order.getTotal().doubleValue())
                    : "0 VNĐ";

            data.put("orderNumber", order.getOrderNumber());
            data.put("status", order.getStatus() != null ? order.getStatus().name() : "PENDING");
            data.put("statusDescription", statusDesc);
            data.put("totalAmount", order.getTotal());
            data.put("formattedTotal", formattedTotal);

            // Bảo vệ quyền riêng tư: Che họ tên và địa chỉ đối với khách không phải chủ sở hữu tài khoản / Admin
            if (isFullDetails) {
                data.put("customerName", order.getFullName());
                data.put("maskedPhone", maskPhone(order.getPhone()));
                data.put("address", (order.getAddress() != null ? order.getAddress() + ", " : "") + order.getWard() + ", " + order.getProvince());
            } else {
                data.put("customerName", maskName(order.getFullName()));
                data.put("maskedPhone", maskPhone(order.getPhone()));
                data.put("address", order.getWard() + ", " + order.getProvince());
            }

            data.put("paymentMethod", order.getPaymentMethod());
            data.put("paymentStatus", order.getPaymentStatus());
            data.put("shippingMethod", order.getShippingMethod());
            data.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");

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
            data.put("items", items);
            data.put("totalItems", items.size());

            String message = String.format("Đơn hàng %s đang ở trạng thái: '%s'. Tổng giá trị: %s. Hình thức: %s (%s).",
                    order.getOrderNumber(), statusDesc, formattedTotal, order.getPaymentMethod(), order.getPaymentStatus());
            data.put("message", message);

            return buildSuccessResult(message, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi tra cứu đơn hàng: " + e.getMessage());
        } finally {
            Map<String, Object> loggedParams = new HashMap<>();
            loggedParams.put("orderCode", orderCode != null ? orderCode : "");
            if (phoneNumber != null) {
                loggedParams.put("phone", maskPhone(phoneNumber));
            }
            logToolExecution(sessionId, currentUserId, "track_order", loggedParams, data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 9: Tìm linh kiện theo vai trò trong dự án
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findProjectComponents(String projectType, List<String> componentRoles, Map<String, Object> constraints, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            Map<String, Object> data = projectPlanningService.findProjectComponents(projectType, componentRoles, constraints);
            String msg = String.format("Đã rà soát %d vai trò linh kiện cho dự án '%s' (%d vai trò có sẵn trong kho).",
                    data.get("totalRolesExamined"), projectType, data.get("rolesAvailableInShop"));
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi tìm linh kiện theo vai trò: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "find_project_components",
                    Map.of("projectType", projectType != null ? projectType : ""), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 10: Kiểm tra tính tương thích giữa các linh kiện được chọn
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkComponentCompatibility(String projectType, List<Map<String, Object>> components, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            Map<String, Object> data = projectPlanningService.checkCompatibility(projectType, components);
            String msg = (String) data.getOrDefault("summary", "Đã kiểm tra tương thích kỹ thuật.");
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi kiểm tra tính tương thích: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "check_component_compatibility",
                    Map.of("projectType", projectType != null ? projectType : ""), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 11: Lập bảng dự toán danh mục linh kiện (BOM) hoàn chỉnh cho dự án
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buildBillOfMaterials(String projectType, Map<String, Object> constraints, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            Map<String, Object> data = projectPlanningService.buildBillOfMaterials(projectType, constraints);
            String msg = String.format("Đã lập bảng BOM cho dự án '%s': %d linh kiện tại shop (%s), %d linh kiện mua ngoài (%s). Tổng chi phí dự tính: %s.",
                    projectType, data.get("shopItemCount"), data.get("formattedShopTotal"),
                    data.get("missingItemCount"), data.get("formattedExternalEstimatedTotal"), data.get("formattedGrandTotal"));
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi lập bảng BOM dự án: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "build_bill_of_materials",
                    Map.of("projectType", projectType != null ? projectType : ""), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 12: Phân tích đơn hàng chuyên sâu (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderAnalytics(String startDate, String endDate, String period, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_order_analytics", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }
            Map<String, Object> data = adminAnalyticsService.getOrderAnalytics(startDate, endDate, period);
            String msg = (String) data.getOrDefault("summary", "Đã xuất báo cáo phân tích đơn hàng.");
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi phân tích đơn hàng: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_order_analytics",
                    Map.of("startDate", startDate != null ? startDate : "", "endDate", endDate != null ? endDate : ""), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 13: Báo cáo tồn kho toàn diện & Dự báo nhập hàng (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryReport(Integer threshold, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_inventory_report", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }
            Map<String, Object> data = adminAnalyticsService.getInventoryReport(threshold);
            String msg = (String) data.getOrDefault("summary", "Đã xuất báo cáo tồn kho toàn diện.");
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi lập báo cáo tồn kho: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_inventory_report",
                    Map.of("threshold", threshold != null ? threshold : 10), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 14: Hiệu suất & Xếp hạng sản phẩm theo doanh số và đánh giá (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductPerformance(Integer limit, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_product_performance", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }
            Map<String, Object> data = adminAnalyticsService.getProductPerformance(limit);
            String msg = (String) data.getOrDefault("summary", "Đã tổng hợp hiệu suất kinh doanh sản phẩm.");
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi tổng hợp hiệu suất sản phẩm: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_product_performance",
                    Map.of("limit", limit != null ? limit : 5), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 15: Phân tích khách hàng & Tỷ lệ giữ chân (Yêu cầu role ADMIN)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerAnalytics(String startDate, String endDate, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_customer_analytics", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }
            Map<String, Object> data = adminAnalyticsService.getCustomerAnalytics(startDate, endDate);
            String msg = (String) data.getOrDefault("summary", "Đã phân tích số liệu khách hàng.");
            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi phân tích khách hàng: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_customer_analytics",
                    Map.of("startDate", startDate != null ? startDate : "", "endDate", endDate != null ? endDate : ""), null, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 16: Báo cáo phân tích chuyên sâu các chủ đề, linh kiện và nhu cầu mà khách hàng thường hỏi qua chatbot và website (Chỉ dành cho Admin).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerInquiryAnalytics(Integer limit, String userRole, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            var accessDecision = aiToolPolicyService.checkToolAccess("get_customer_inquiry_analytics", userRole, userId);
            if (!accessDecision.isAllowed()) {
                return buildErrorResult(accessDecision.getReason());
            }

            data = adminAnalyticsService.getCustomerInquiryAnalytics(limit);
            return buildSuccessResult((String) data.get("summary"), data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Lỗi khi phân tích nhu cầu và chủ đề người dùng: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "get_customer_inquiry_analytics",
                    Map.of("limit", limit != null ? limit : 5),
                    data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    /**
     * Tool 17: Tra cứu thông tin bảo hành của linh kiện theo Mã Serial / Tem bảo hành
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkWarranty(String serialNumber, String phoneNumber, UUID sessionId, Long userId) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String error = null;
        Map<String, Object> data = new HashMap<>();

        try {
            if (serialNumber == null || serialNumber.isBlank()) {
                return buildErrorResult("Vui lòng cung cấp mã Serial để kiểm tra bảo hành.");
            }
            var res = warrantyService.lookupWarranty(serialNumber, phoneNumber);

            data.put("type", "WARRANTY_LOOKUP");
            data.put("serialNumber", res.getSerialNumber());
            data.put("productName", res.getProductName());
            data.put("variantName", res.getVariantName());
            data.put("productImageUrl", res.getProductImageUrl());
            data.put("orderCode", res.getOrderCode());
            data.put("status", res.getStatus());
            data.put("isExpired", res.isExpired());
            data.put("daysRemaining", res.getDaysRemaining());
            data.put("warrantyMonths", res.getWarrantyMonths());
            data.put("activatedAt", res.getActivatedAt() != null ? res.getActivatedAt().toString() : null);
            data.put("expiresAt", res.getExpiresAt() != null ? res.getExpiresAt().toString() : null);
            data.put("claimHistory", res.getClaimHistory());

            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String expStr = res.getExpiresAt() != null ? res.getExpiresAt().format(dtf) : "Không xác định";
            String actStr = res.getActivatedAt() != null ? res.getActivatedAt().format(dtf) : "Không xác định";

            String msg = res.isExpired()
                    ? String.format("Linh kiện [%s] (Mã tem %s) ĐÃ HẾT HẠN BẢO HÀNH từ ngày %s. Nếu linh kiện gặp sự cố, bạn có thể liên hệ bộ phận hỗ trợ kỹ thuật để sửa chữa dịch vụ.",
                    res.getProductName(), res.getSerialNumber(), expStr)
                    : String.format("Linh kiện [%s] (Mã tem %s) ĐANG TRONG HẠN BẢO HÀNH. Kích hoạt: %s | Hết hạn: %s (Còn lại %d ngày). Bạn có thể gửi yêu cầu bảo hành tại trang web hoặc kiểm tra chi tiết.",
                    res.getProductName(), res.getSerialNumber(), actStr, expStr, res.getDaysRemaining());

            return buildSuccessResult(msg, data);
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            return buildErrorResult("Không thể tra cứu bảo hành: " + e.getMessage());
        } finally {
            logToolExecution(sessionId, userId, "check_warranty",
                    Map.of("serialNumber", serialNumber != null ? serialNumber : ""),
                    data, status, System.currentTimeMillis() - startTime, error);
        }
    }

    private String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Khách hàng";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].charAt(0) + "***";
        return parts[0] + " " + parts[parts.length - 1].charAt(0) + "***";
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    private String getStatusDescription(Order.OrderStatus status) {
        if (status == null) return "Chờ xử lý";
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận / Đang chuẩn bị hàng";
            case SHIPPED -> "Đang giao hàng";
            case DELIVERED -> "Đã giao thành công";
            case RETURNED -> "Đã trả hàng / Hoàn tiền";
            case CANCELLED -> "Đã hủy đơn";
        };
    }

    private LocalDate parseDateFlexible(String dateStr, boolean isStart) {
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

    private BigDecimal effectivePrice(ProductWithVariantsDTO product) {
        return product.getMinDiscountPrice() != null ? product.getMinDiscountPrice() : product.getMinPrice();
    }

    private int recommendationScore(ProductWithVariantsDTO product, List<String> keywords) {
        String searchable = normalizeText(product.getName() + " "
                + product.getCategories().stream().map(category -> category.getName() + " " + category.getSlug()).reduce("", (a, b) -> a + " " + b)
                + " " + product.getVariants().stream()
                .map(variant -> Objects.toString(variant.getDescription(), "") + " " + Objects.toString(variant.getSpecifications(), ""))
                .reduce("", (a, b) -> a + " " + b));
        return (int) keywords.stream().filter(searchable::contains).count();
    }

    private List<String> recommendationKeywords(String request) {
        String cleaned = normalizeText(request)
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*(?:k|nghin|ngan|tr|trieu|dong|d)?", " ")
                .replaceAll("\\b(duoi|toi da|ngan sach|tam|khoang|goi y|tu van|tim|tim kiem|san pham|nao|phu hop|cho toi|toi can|minh can|minh muon|muon|can|voi|gia|ngan|hang|con|khong|co|la|de|lam|mot|cai|loai|re|tot|nhat|duoc)\\b", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(cleaned.split("\\s+"))
                .filter(word -> word.length() >= 2)
                .distinct()
                .toList();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", amount);
    }

    private String normalizeProductQuery(String message) {
        return normalizeText(message)
                .replaceAll("\\b(so sanh|con hang khong|con hang|con khong|co hang khong|co con khong|bao nhieu tien|gia ban|gia bao nhieu|gia|ton kho|kiem tra|tra cuu|tu van|cho toi biet|giup toi|voi|shop oi|shop|duoi|toi da|ngan sach|goi y|phu hop)\\b", " ")
                .replaceAll("[?!.,:;]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeText(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
    }

    /**
     * Trả về danh sách đặc tả công cụ (Tool Definitions) đầy đủ cho toàn bộ 17 công cụ của hệ thống
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return getToolDefinitions("ROLE_ADMIN");
    }

    /**
     * Trả về danh sách công cụ theo phân quyền vai trò (Role-Based Tool Catalog).
     * Áp dụng nguyên tắc Least Privilege: Khách và User thông thường không bao giờ
     * được cấp schema của các công cụ quản trị (revenue, analytics, inventory value,...).
     */
    public List<Map<String, Object>> getToolDefinitions(String userRole) {
        boolean isAdmin = aiToolPolicyService.isAdmin(userRole);
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

        // Tool 2: recommend_products
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "recommend_products",
                        "description", "Gợi ý các linh kiện điện tử phù hợp với nhu cầu sử dụng hoặc dự án kỹ thuật, hỗ trợ lọc theo ngân sách tối đa.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "Mục đích sử dụng, tên dự án hoặc loại linh kiện cần gợi ý (vd: làm xe dò line, làm trạm quan trắc IoT, đo nhiệt độ)"
                                        ),
                                        "max_budget", Map.of(
                                                "type", "number",
                                                "description", "Ngân sách tối đa tính bằng VNĐ (vd: 200000, 500000). Có thể bỏ trống nếu không giới hạn ngân sách."
                                        )
                                ),
                                "required", List.of("query")
                        )
                )
        ));

        // Tool 3: compare_products
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "compare_products",
                        "description", "So sánh thông số kỹ thuật, giá cả, và tình trạng tồn kho giữa hai hoặc nhiều sản phẩm linh kiện điện tử.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "product_names", Map.of(
                                                "type", "array",
                                                "items", Map.of("type", "string"),
                                                "description", "Danh sách tên các linh kiện cần so sánh (vd: [\"Arduino Uno R3\", \"Raspberry Pi 5\"])"
                                        )
                                ),
                                "required", List.of("product_names")
                        )
                )
        ));

        // Tool 4: check_stock
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

        // Tool 5: get_low_stock_products (Admin only)
        if (isAdmin) {
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_low_stock_products",
                            "description", "Tra cứu danh sách các linh kiện có số lượng tồn kho thấp hơn hoặc bằng ngưỡng quy định để cảnh báo nhập hàng. Yêu cầu quyền Quản trị viên (Admin).",
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
        }

        // Tool 6: get_top_selling_products
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

        // Tool 7: get_revenue_report (Admin only)
        if (isAdmin) {
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
                                    "required", List.of()
                            )
                    )
            ));
        }

        // Tool 8: track_order
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "track_order",
                        "description", "Kiểm tra tình trạng vận chuyển, trạng thái xử lý, tổng tiền và phương thức thanh toán của đơn hàng theo mã đơn. Nếu là khách vãng lai, cần cung cấp kèm số điện thoại để xác thực quyền sở hữu đơn hàng.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "order_code", Map.of(
                                                "type", "string",
                                                "description", "Mã đơn hàng cần tra cứu (vd: ORD-12345, #ORD-12345,...)"
                                        ),
                                        "phone_number", Map.of(
                                                "type", "string",
                                                "description", "Số điện thoại đặt hàng để xác minh (bắt buộc đối với khách chưa đăng nhập hoặc tra cứu đơn hàng)"
                                        )
                                ),
                                "required", List.of("order_code")
                        )
                )
        ));

        // Tool 9: find_project_components
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "find_project_components",
                        "description", "Tìm kiếm các linh kiện trong kho hàng theo từng vai trò cụ thể trong một dự án kỹ thuật (ví dụ: khung drone, motor, esc, vi điều khiển, cảm biến, nguồn).",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "project_type", Map.of(
                                                "type", "string",
                                                "description", "Tên loại dự án (vd: mini_drone, obstacle_avoiding_robot, line_following_robot, weather_station)"
                                        ),
                                        "component_roles", Map.of(
                                                "type", "array",
                                                "items", Map.of("type", "string"),
                                                "description", "Danh sách các vai trò linh kiện cần tìm (vd: [\"motor\", \"esc\", \"battery\"])"
                                        )
                                ),
                                "required", List.of("project_type")
                        )
                )
        ));

        // Tool 10: check_component_compatibility
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "check_component_compatibility",
                        "description", "Kiểm tra tính tương thích về mặt điện áp, dòng điện, kích thước khung cánh, và chuẩn giao tiếp giữa các linh kiện được chọn.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "project_type", Map.of(
                                                "type", "string",
                                                "description", "Loại dự án (vd: mini_drone, robot, iot)"
                                        ),
                                        "components", Map.of(
                                                "type", "array",
                                                "items", Map.of("type", "object"),
                                                "description", "Danh sách linh kiện cần kiểm tra tương thích kèm thông số kỹ thuật"
                                        )
                                ),
                                "required", List.of("project_type", "components")
                        )
                )
        ));

        // Tool 11: build_bill_of_materials
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "build_bill_of_materials",
                        "description", "Lập bảng danh mục linh kiện (BOM) hoàn chỉnh cho dự án: tự động chọn linh kiện sẵn có trong kho, tính tổng chi phí, phát hiện các linh kiện shop chưa bán và đưa ra đề xuất mua ngoài.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "project_type", Map.of(
                                                "type", "string",
                                                "description", "Loại dự án kỹ thuật (vd: mini_drone, obstacle_avoiding_robot, line_following_robot, weather_station)"
                                        )
                                ),
                                "required", List.of("project_type")
                        )
                )
        ));

        // Admin Analytics Tools (Tools 12-16: Admin only)
        if (isAdmin) {
            // Tool 12: get_order_analytics
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_order_analytics",
                            "description", "Báo cáo phân tích chuyên sâu tình trạng đơn hàng: tỷ lệ hủy, tỷ lệ hoàn trả, giá trị đơn hàng trung bình (AOV), doanh thu theo trạng thái và so sánh tăng trưởng kỳ trước. Yêu cầu quyền Quản trị viên (Admin).",
                            "parameters", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "start_date", Map.of("type", "string", "description", "Ngày bắt đầu YYYY-MM-DD"),
                                            "end_date", Map.of("type", "string", "description", "Ngày kết thúc YYYY-MM-DD"),
                                            "period", Map.of("type", "string", "description", "Khoảng thời gian: day, month, year hoặc range")
                                    )
                            )
                    )
            ));

            // Tool 13: get_inventory_report
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_inventory_report",
                            "description", "Báo cáo tổng giá trị vốn lưu kho, danh sách mã hết hàng, mã sắp cạn kho, hàng tồn lâu chậm luân chuyển và đề xuất số lượng nhập thêm cho mặt hàng bán chạy. Yêu cầu quyền Quản trị viên (Admin).",
                            "parameters", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "threshold", Map.of("type", "integer", "description", "Ngưỡng tồn kho tối thiểu (mặc định 10)")
                                    )
                            )
                    )
            ));

            // Tool 14: get_product_performance
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_product_performance",
                            "description", "Xếp hạng hiệu suất sản phẩm theo lượt bán chạy, đánh giá sao của khách hàng và tỷ trọng doanh thu theo từng danh mục. Yêu cầu quyền Quản trị viên (Admin).",
                            "parameters", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "limit", Map.of("type", "integer", "description", "Số lượng sản phẩm lấy (mặc định 5 hoặc 10)")
                                    )
                            )
                    )
            ));

            // Tool 15: get_customer_analytics
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_customer_analytics",
                            "description", "Phân tích khách hàng: số lượng đăng ký mới, khách hàng phát sinh đơn hàng, và tỷ lệ khách hàng mua lại (retention rate). Yêu cầu quyền Quản trị viên (Admin).",
                            "parameters", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "start_date", Map.of("type", "string", "description", "Ngày bắt đầu YYYY-MM-DD"),
                                            "end_date", Map.of("type", "string", "description", "Ngày kết thúc YYYY-MM-DD")
                                    )
                            )
                    )
            ));

            // Tool 16: get_customer_inquiry_analytics
            tools.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", "get_customer_inquiry_analytics",
                            "description", "Báo cáo phân tích chuyên sâu các chủ đề kỹ thuật, dự án DIY và linh kiện mà khách hàng thường hỏi nhiều nhất qua chatbot và thanh tìm kiếm website, kèm cảnh báo nhu cầu chưa đáp ứng (hàng hết hoặc chưa có). Yêu cầu quyền Quản trị viên (Admin).",
                            "parameters", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "limit", Map.of("type", "integer", "description", "Số lượng top mục cần thống kê (mặc định 5)")
                                    )
                            )
                    )
            ));
        }

        // Tool 17: check_warranty
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "check_warranty",
                        "description", "Tra cứu thời hạn bảo hành của linh kiện điện tử dựa trên mã Serial / mã tem bảo hành (ví dụ: SN-2610-A1B2C3D4) và số điện thoại người mua. Áp dụng cho mọi đối tượng khách hàng.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "serial_number", Map.of("type", "string", "description", "Mã serial hoặc mã tem bảo hành dán trên linh kiện"),
                                        "phone_number", Map.of("type", "string", "description", "Số điện thoại của người mua hàng (tùy chọn)")
                                ),
                                "required", List.of("serial_number")
                        )
                )
        ));

        return tools;
    }

    private void logToolExecution(UUID sessionId, Long userId, String toolName, Object args, Object result, String status, long durationMs, String errorCode) {
        try {
            String argsJson = objectMapper.writeValueAsString(args);
            String resultJson = objectMapper.writeValueAsString(result);
            // Bảo mật PII: Khử trùng số điện thoại, email, địa chỉ người nhận trước khi ghi audit log
            String sanitizedArgs = aiSecuritySanitizer != null ? aiSecuritySanitizer.maskPii(argsJson) : argsJson;
            String sanitizedResult = aiSecuritySanitizer != null ? aiSecuritySanitizer.maskPii(resultJson) : resultJson;

            ChatToolLog logEntry = ChatToolLog.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .toolName(toolName)
                    .toolArguments(sanitizedArgs)
                    .toolResult(sanitizedResult)
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
