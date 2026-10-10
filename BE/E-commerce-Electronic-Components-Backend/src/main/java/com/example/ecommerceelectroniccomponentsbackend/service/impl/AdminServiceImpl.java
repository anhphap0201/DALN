package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.AdminOrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminStatsDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminUserDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.DashboardDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderItemDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.repository.CategoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements IAdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ==================== STATS ====================

    @Transactional(readOnly = true)
    public AdminStatsDTO getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();

        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(Order.OrderStatus.CONFIRMED);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orderRepository.sumTotalCostByStatus(Order.OrderStatus.DELIVERED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Recent 5 orders
        Page<Order> recentPage = orderRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AdminOrderDTO> recentOrders = recentPage.getContent().stream()
                .map(this::toAdminOrderDTO)
                .collect(Collectors.toList());

        return AdminStatsDTO.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .recentOrders(recentOrders)
                .build();
    }

    // ==================== CHART DATA ====================

    @Transactional(readOnly = true)
    public List<DashboardDTO.DailyStats> getChartData(int days) {
        return buildRevenueChart(LocalDate.now(), days);
    }

    // ==================== DASHBOARD CHI TIẾT ====================

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard() {
        LocalDate now = LocalDate.now();

        // --- Tổng quan ---
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalCostByStatus(Order.OrderStatus.DELIVERED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // --- Trạng thái đơn hàng ---
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(Order.OrderStatus.CONFIRMED);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        // --- Hôm nay ---
        LocalDateTime todayStart = now.atStartOfDay();
        LocalDateTime todayEnd = now.plusDays(1).atStartOfDay();
        DashboardDTO.PeriodStats today = buildPeriodStats(todayStart, todayEnd);

        // --- Tuần này (Thứ 2 -> CN) ---
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();
        DashboardDTO.PeriodStats thisWeek = buildPeriodStats(weekStart, weekEnd);

        // --- Tháng này ---
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        DashboardDTO.PeriodStats thisMonth = buildPeriodStats(monthStart, monthEnd);

        // --- Năm này ---
        LocalDateTime yearStart = now.withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd = now.plusYears(1).withDayOfYear(1).atStartOfDay();
        DashboardDTO.PeriodStats thisYear = buildPeriodStats(yearStart, yearEnd);

        // --- Biểu đồ doanh thu 30 ngày gần đây ---
        List<DashboardDTO.DailyStats> revenueChart = buildRevenueChart(now, 30);

        // --- Top 10 sản phẩm bán chạy (30 ngày) ---
        List<DashboardDTO.TopProduct> topProducts = buildTopProducts(
                now.minusDays(30).atStartOfDay(), todayEnd, 10);

        // --- Thống kê sản phẩm (avg_rating & sold_quantity) ---
        DashboardDTO.ProductStats productStats = buildProductStats();

        // --- Đơn hàng gần đây ---
        Page<Order> recentPage = orderRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AdminOrderDTO> recentOrders = recentPage.getContent().stream()
                .map(this::toAdminOrderDTO)
                .collect(Collectors.toList());

        return DashboardDTO.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .today(today)
                .thisWeek(thisWeek)
                .thisMonth(thisMonth)
                .thisYear(thisYear)
                .revenueChart(revenueChart)
                .topProducts(topProducts)
                .productStats(productStats)
                .recentOrders(recentOrders)
                .build();
    }

    private DashboardDTO.PeriodStats buildPeriodStats(LocalDateTime from, LocalDateTime to) {
        long orderCount = orderRepository.countByCreatedAtBetween(from, to);
        BigDecimal revenue = orderRepository.sumRevenueByPeriod(from, to);
        if (revenue == null) revenue = BigDecimal.ZERO;
        long newUsers = userRepository.countByCreatedAtBetween(from, to);
        return DashboardDTO.PeriodStats.builder()
                .orderCount(orderCount)
                .revenue(revenue)
                .newUsers(newUsers)
                .build();
    }

    private List<DashboardDTO.DailyStats> buildRevenueChart(LocalDate today, int days) {
        LocalDateTime from = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        // Lấy tất cả đơn DELIVERED trong khoảng thời gian
        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Group theo ngày
        Map<String, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate().format(fmt)));

        List<DashboardDTO.DailyStats> chart = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(fmt);
            List<Order> dayOrders = grouped.getOrDefault(dateStr, List.of());

            long orderCount = dayOrders.size();
            BigDecimal revenue = dayOrders.stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                    .map(Order::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            chart.add(DashboardDTO.DailyStats.builder()
                    .date(dateStr)
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }
        return chart;
    }

    private List<DashboardDTO.TopProduct> buildTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        List<Object[]> rows = orderRepository.findTopSellingProducts(from, to, PageRequest.of(0, limit));
        return rows.stream().map(row -> DashboardDTO.TopProduct.builder()
                .productId(((Number) row[0]).longValue())
                .productName((String) row[1])
                .variantName((String) row[2])
                .totalQuantity(((Number) row[3]).longValue())
                .totalRevenue((BigDecimal) row[4])
                .build()
        ).collect(Collectors.toList());
    }

    private DashboardDTO.ProductStats buildProductStats() {
        long totalSoldQuantity = productRepository.sumTotalSoldQuantity();
        double averageRating = productRepository.averageRating();

        // Top 10 sản phẩm bán chạy nhất (theo sold_quantity)
        List<Product> topSelling = productRepository.findTop10ByOrderBySoldQuantityDesc();
        List<DashboardDTO.ProductRankItem> topSellingProducts = topSelling.stream()
                .map(p -> DashboardDTO.ProductRankItem.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .slug(p.getSlug())
                        .soldQuantity(p.getSoldQuantity() != null ? p.getSoldQuantity() : 0)
                        .avgRating(p.getAvgRating() != null ? p.getAvgRating() : 0)
                        .build())
                .collect(Collectors.toList());

        // Top 10 sản phẩm đánh giá cao nhất
        List<Product> topRated = productRepository.findTopRatedProducts(PageRequest.of(0, 10));
        List<DashboardDTO.ProductRankItem> topRatedProducts = topRated.stream()
                .map(p -> DashboardDTO.ProductRankItem.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .slug(p.getSlug())
                        .soldQuantity(p.getSoldQuantity() != null ? p.getSoldQuantity() : 0)
                        .avgRating(p.getAvgRating() != null ? p.getAvgRating() : 0)
                        .build())
                .collect(Collectors.toList());

        // Phân bố đánh giá
        DashboardDTO.RatingDistribution ratingDistribution = DashboardDTO.RatingDistribution.builder()
                .fiveStar(productRepository.countByAvgRatingBetween(4.5, 5.0))
                .fourStar(productRepository.countByAvgRatingBetween(3.5, 4.4999))
                .threeStar(productRepository.countByAvgRatingBetween(2.5, 3.4999))
                .twoStar(productRepository.countByAvgRatingBetween(1.5, 2.4999))
                .oneStar(productRepository.countByAvgRatingBetween(0.1, 1.4999))
                .noRating(productRepository.countByAvgRating(0))
                .build();

        return DashboardDTO.ProductStats.builder()
                .totalSoldQuantity(totalSoldQuantity)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .topSellingProducts(topSellingProducts)
                .topRatedProducts(topRatedProducts)
                .ratingDistribution(ratingDistribution)
                .build();
    }

    // ==================== ORDERS ====================

    @Transactional(readOnly = true)
    public Page<AdminOrderDTO> getAllOrders(Pageable pageable, String status, String search) {
        if (status != null && !status.isEmpty() && search != null && !search.isEmpty()) {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatusAndSearch(orderStatus, search, pageable)
                    .map(this::toAdminOrderDTO);
        } else if (status != null && !status.isEmpty()) {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus, pageable)
                    .map(this::toAdminOrderDTO);
        } else if (search != null && !search.isEmpty()) {
            return orderRepository.findBySearch(search, pageable)
                    .map(this::toAdminOrderDTO);
        }
        return orderRepository.findAll(pageable).map(this::toAdminOrderDTO);
    }

    @Transactional(readOnly = true)
    public AdminOrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        return toAdminOrderDTO(order);
    }

    @Transactional
    public AdminOrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Update timestamps based on status
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
            default -> {}
        }

        Order updated = orderRepository.save(order);
        log.info("Admin updated order {} status to {}", orderId, status);
        return toAdminOrderDTO(updated);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
        log.info("Admin deleted order {}", orderId);
    }

    // ==================== USERS ====================

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(Pageable pageable, String search) {
        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findBySearch(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::toAdminUserDTO);
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return toAdminUserDTO(user);
    }

    @Transactional
    public AdminUserDTO toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        log.info("Admin toggled user {} active status to {}", userId, user.getIsActive());
        return toAdminUserDTO(user);
    }

    // ==================== MAPPERS ====================

    private AdminOrderDTO toAdminOrderDTO(Order order) {
        return AdminOrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalCost(order.getTotalCost())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discount(order.getDiscount())
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .province(order.getProvince())
                .ward(order.getWard())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingMethod(order.getShippingMethod())
                .note(order.getNote())
                .voucherCode(order.getVoucherCode())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .orderItems(order.getOrderItems().stream()
                        .map(this::toOrderItemDTO)
                        .collect(Collectors.toSet()))
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant().getId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .variantName(item.getProductVariant().getVariantName())
                .build();
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        long orderCount = orderRepository.countByUserId(user.getId());
        BigDecimal totalSpent = orderRepository.sumTotalCostByUserIdAndStatus(user.getId(), Order.OrderStatus.DELIVERED);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .province(user.getProvince())
                .ward(user.getWard())
                .detailedAddress(user.getDetailedAddress())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .build();
    }
}
