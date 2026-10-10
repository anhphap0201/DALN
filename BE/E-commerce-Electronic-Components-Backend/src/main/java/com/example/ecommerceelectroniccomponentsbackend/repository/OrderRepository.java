package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
       Optional<Order> findByLockerOrderId(String lockerOrderId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.productVariant WHERE o.id = :orderId AND o.userId = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // Admin queries
    long countByStatus(Order.OrderStatus status);
    long countByUserId(long userId);

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> findByStatusAndSearch(@Param("status") Order.OrderStatus status,
                                      @Param("search") String search,
                                      Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Order> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalCost), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalCostByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalCost), 0) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    BigDecimal sumTotalCostByUserIdAndStatus(@Param("userId") long userId, @Param("status") Order.OrderStatus status);

    // ==================== Dashboard time-based queries ====================

    // Đếm đơn hàng theo khoảng thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Doanh thu (đơn DELIVERED) theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(o.totalCost), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :from AND o.createdAt < :to")
    BigDecimal sumRevenueByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Đơn hàng trong khoảng thời gian (cho biểu đồ)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to ORDER BY o.createdAt ASC")
    List<Order> findByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Top sản phẩm bán chạy
    @Query("SELECT oi.productVariant.product.id, oi.productVariant.product.name, oi.productVariant.variantName, " +
           "SUM(oi.quantity), COALESCE(SUM(oi.price * oi.quantity), 0) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt >= :from AND o.createdAt < :to " +
           "GROUP BY oi.productVariant.product.id, oi.productVariant.product.name, oi.productVariant.variantName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
}
