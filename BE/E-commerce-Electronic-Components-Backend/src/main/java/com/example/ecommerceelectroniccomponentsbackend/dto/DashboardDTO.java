package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    // --- Tổng quan ---
    private long totalOrders;
    private long totalUsers;
    private long totalProducts;
    private long totalCategories;
    private BigDecimal totalRevenue;

    // --- Trạng thái đơn hàng ---
    private long pendingOrders;
    private long confirmedOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;

    // --- Doanh thu & đơn hàng theo khoảng thời gian ---
    private PeriodStats today;
    private PeriodStats thisWeek;
    private PeriodStats thisMonth;
    private PeriodStats thisYear;

    // --- Doanh thu theo ngày gần đây (biểu đồ) ---
    private List<DailyStats> revenueChart;

    // --- Top sản phẩm bán chạy ---
    private List<TopProduct> topProducts;

    // --- Thống kê sản phẩm (avg_rating & sold_quantity) ---
    private ProductStats productStats;

    // --- Đơn hàng gần đây ---
    private List<AdminOrderDTO> recentOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodStats {
        private long orderCount;
        private BigDecimal revenue;
        private long newUsers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStats {
        private String date;       // yyyy-MM-dd
        private long orderCount;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String variantName;
        private long totalQuantity;
        private BigDecimal totalRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductStats {
        private long totalSoldQuantity;
        private double averageRating;
        private List<ProductRankItem> topSellingProducts;
        private List<ProductRankItem> topRatedProducts;
        private RatingDistribution ratingDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductRankItem {
        private Long productId;
        private String productName;
        private String slug;
        private int soldQuantity;
        private double avgRating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingDistribution {
        private long fiveStar;   // 4.5 - 5.0
        private long fourStar;   // 3.5 - 4.5
        private long threeStar;  // 2.5 - 3.5
        private long twoStar;    // 1.5 - 2.5
        private long oneStar;    // 0.1 - 1.5
        private long noRating;   // 0
    }
}
