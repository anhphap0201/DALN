package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.ChatToolLogRepository;
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
