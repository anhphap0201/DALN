package com.example.ecommerceelectroniccomponentsbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"orderItems"})
@ToString(exclude = {"orderItems"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // --- Money fields ---
    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "discount")
    private BigDecimal discount;

    // --- Customer info ---
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    // --- Address fields ---
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "province", nullable = false)
    private String province;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "ward", nullable = false)
    private String ward;

    @Column(name = "ward_code", nullable = false)
    private String wardCode;

    // --- Order details ---
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "shipping_method", nullable = false)
    private String shippingMethod;

    @Column(name = "note")
    private String note;

    @Column(name = "voucher_code")
    private String voucherCode;

    // --- Smart Locker fields ---
    @Column(name = "locker_id")
    private String lockerId;

    @Column(name = "compartment_id")
    private String compartmentId;

    @Column(name = "locker_order_id")
    private String lockerOrderId;

    @Column(name = "sender_otp")
    private String senderOTP;

    @Column(name = "recipient_otp")
    private String recipientOTP;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    // --- Timestamps ---
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // --- Items ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
