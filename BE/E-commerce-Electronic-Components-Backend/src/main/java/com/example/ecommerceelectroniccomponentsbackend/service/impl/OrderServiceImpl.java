package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.DeliveryWebhookRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.SmartLockerDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Cart;
import com.example.ecommerceelectroniccomponentsbackend.entity.CartItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.mapper.OrderMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.CartRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IOrderService;
import com.example.ecommerceelectroniccomponentsbackend.service.ISmartLockerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ISmartLockerService smartLockerService;

    @Transactional
    public OrderDTO createOrderFromCart(Long userId, CreateOrderRequest request) {
        log.info("Creating order from cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate user profile
        validateUserProfile(user);

        // Build address and phone from user profile
        String shippingAddress = buildShippingAddress(user);
        String phone = safeTrim(user.getPhone());
        String province = safeTrim(user.getProvince());
        String ward = safeTrim(user.getWard());

        // Calculate totals
        BigDecimal itemsSubtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            itemsSubtotal = itemsSubtotal.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalTotal = itemsSubtotal.add(shippingFee).subtract(discount);

        // Generate unique order number
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Build order with all required fields
        Order order = Order.builder()
                .userId(userId)
                .status(Order.OrderStatus.PENDING)
                // Money fields - populate all with calculated values (schema requires all NOT NULL)
                .totalCost(finalTotal)
                .totalPrice(finalTotal)
                .subtotal(itemsSubtotal)
                .total(finalTotal)
                .shippingFee(shippingFee)
                .shippingCost(shippingFee)
                .discount(discount)
                // Customer info
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(phone)
                // Address
                .address(shippingAddress)
                .province(province)
                .provinceCode("") // User model doesn't have province code
                .ward(ward)
                .wardCode("") // User model doesn't have ward code
                // Order details
                .orderNumber(orderNumber)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .paymentStatus("PENDING")
                .shippingMethod(request.getShippingMethod() != null ? request.getShippingMethod().toUpperCase() : "STANDARD")
                .note(request.getNote())
                .voucherCode(request.getVoucherCode())
                .build();

        // Add order items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(cartItem.getProductVariant())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
        }

        // Smart Locker integration
        String shippingMethodValue = request.getShippingMethod() != null ? request.getShippingMethod().toUpperCase() : "STANDARD";
        if ("SMART_LOCKER".equals(shippingMethodValue)) {
            if (request.getLockerId() == null) {
                throw new IllegalArgumentException("Locker ID is required for smart locker delivery");
            }

            order.setLockerId(request.getLockerId());

            // Build locker order items
            List<SmartLockerDTO.LockerOrderItem> lockerItems = cart.getCartItems().stream()
                    .map(ci -> SmartLockerDTO.LockerOrderItem.builder()
                            .name(ci.getProductVariant().getVariantName())
                            .quantity(ci.getQuantity())
                            .build())
                    .toList();

            SmartLockerDTO.LockerOrderRequest lockerRequest = SmartLockerDTO.LockerOrderRequest.builder()
                    .userId(String.valueOf(userId))
                    .lockerId(request.getLockerId())
                    .orderInfo(SmartLockerDTO.LockerOrderInfo.builder()
                            .items(lockerItems)
                            .totalPrice(finalTotal.longValue())
                            .notes(request.getNote())
                            .build())
                    .build();

            try {
                SmartLockerDTO.LockerOrderResponse lockerResponse = smartLockerService.createLockerOrder(lockerRequest);
                order.setLockerOrderId(lockerResponse.getOrderId());
                order.setCompartmentId(lockerResponse.getCompartmentId());
                order.setSenderOTP(lockerResponse.getSenderOTP());
                order.setRecipientOTP(lockerResponse.getRecipientOTP());
                log.info("Smart locker order created: {}, compartment assigned: {}",
                        lockerResponse.getOrderId(), lockerResponse.getCompartmentId());
            } catch (IllegalStateException e) {
                log.error("Failed to create smart locker order: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Failed to create smart locker order: {}", e.getMessage());
                throw new IllegalStateException("Không thể đặt tủ thông minh. Vui lòng thử lại.");
            }
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and number: {} for user: {}", savedOrder.getId(), savedOrder.getOrderNumber(), userId);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderMapper.toDTO(savedOrder);
    }

    private void validateUserProfile(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required. Please update your profile.");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required. Please update your profile.");
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required. Please update your profile.");
        }
        if (user.getDetailedAddress() == null || user.getDetailedAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Detailed address is required. Please update your profile.");
        }
        if (user.getWard() == null || user.getWard().trim().isEmpty()) {
            throw new IllegalArgumentException("Ward is required. Please update your profile.");
        }
        if (user.getProvince() == null || user.getProvince().trim().isEmpty()) {
            throw new IllegalArgumentException("Province is required. Please update your profile.");
        }
    }

    private String buildShippingAddress(User user) {
        String detailedAddress = safeTrim(user.getDetailedAddress());
        String ward = safeTrim(user.getWard());
        String province = safeTrim(user.getProvince());
        return String.join(", ", detailedAddress, ward, province);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrderHistory(Long userId, Pageable pageable) {
        log.info("Retrieving order history for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, Long userId) {
        log.info("Retrieving order: {} for user: {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or does not belong to user"));
        return orderMapper.toDTO(order);
    }

    @Transactional
    public Map<String, Object> getDeliveryStatusByReference(String orderReference) {
        log.info("Retrieving delivery status for reference: {}", orderReference);

        String normalizedReference = orderReference == null ? "" : orderReference.trim();
        if (normalizedReference.isEmpty()) {
            throw new IllegalArgumentException("order_id is required");
        }

        Order order = resolveOrderByReference(normalizedReference)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        String deliveryStatus = order.getDeliveryStatus();
        if (deliveryStatus == null || deliveryStatus.isBlank()) {
            deliveryStatus = mapInternalStatusToDeliveryStatus(order.getStatus());
        }
        // If order is cancelled, always reflect that
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            deliveryStatus = "cancelled";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", order.getId());
        response.put("orderNumber", order.getOrderNumber());
        response.put("lockerOrderId", order.getLockerOrderId());
        response.put("deliveryStatus", deliveryStatus);
        response.put("status", order.getStatus().name());
        response.put("shippingMethod", order.getShippingMethod());
        response.put("recipientOTP", order.getRecipientOTP());
        response.put("senderOTP", order.getSenderOTP());
        response.put("updatedAt", getEffectiveUpdatedAt(order));
        return response;
    }

    @Transactional
    public Map<String, Object> handleDeliveryWebhook(DeliveryWebhookRequest request) {
        log.info("Processing delivery webhook: orderRef={}, status={}",
                request.getOrderReference(), request.getDeliveryStatus());

        String ref = request.getOrderReference();
        if (ref == null || ref.isBlank()) {
            throw new IllegalArgumentException("orderReference is required");
        }

        Order order = resolveOrderByReference(ref.trim())
                .orElseThrow(() -> new IllegalArgumentException("Order not found for reference: " + ref));

        boolean changed = false;

        // Update delivery status
        if (request.getDeliveryStatus() != null && !request.getDeliveryStatus().isBlank()) {
            order.setDeliveryStatus(request.getDeliveryStatus().toUpperCase());
            changed = true;

            // Sync order status based on delivery status
            String ds = request.getDeliveryStatus().toUpperCase();
            switch (ds) {
                case "CONFIRMED":
                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        order.setStatus(Order.OrderStatus.CONFIRMED);
                        order.setConfirmedAt(LocalDateTime.now());
                    }
                    break;
                case "SHIPPED": case "IN_TRANSIT":
                    if (order.getStatus() != Order.OrderStatus.CANCELLED) {
                        order.setStatus(Order.OrderStatus.SHIPPED);
                        order.setShippedAt(LocalDateTime.now());
                    }
                    break;
                case "DELIVERED": case "DELIVERED_TO_LOCKER": case "PICKED_UP": case "COMPLETED":
                    if (order.getStatus() != Order.OrderStatus.CANCELLED) {
                        order.setStatus(Order.OrderStatus.DELIVERED);
                        order.setDeliveredAt(LocalDateTime.now());
                    }
                    break;
                case "CANCELLED":
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    order.setCancelledAt(LocalDateTime.now());
                    break;
            }
        }

        // Update OTP
        if (request.getSenderOTP() != null && !request.getSenderOTP().isBlank()) {
            order.setSenderOTP(request.getSenderOTP());
            changed = true;
        }
        if (request.getRecipientOTP() != null && !request.getRecipientOTP().isBlank()) {
            order.setRecipientOTP(request.getRecipientOTP());
            changed = true;
        }

        // Update compartment
        if (request.getCompartmentId() != null && !request.getCompartmentId().isBlank()) {
            order.setCompartmentId(request.getCompartmentId());
            changed = true;
        }

        if (changed) {
            orderRepository.save(order);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("status", order.getStatus().name());
        result.put("deliveryStatus", order.getDeliveryStatus());
        return result;
    }

    private java.util.Optional<Order> resolveOrderByReference(String orderReference) {
        if (orderReference.chars().allMatch(Character::isDigit)) {
            try {
                Long orderId = Long.parseLong(orderReference);
                return orderRepository.findById(orderId);
            } catch (NumberFormatException ex) {
                // Fallback to order number lookup below if parsing overflows Long.
            }
        }
        return orderRepository.findByOrderNumber(orderReference)
                .or(() -> orderRepository.findByLockerOrderId(orderReference));
    }

    private LocalDateTime getEffectiveUpdatedAt(Order order) {
        if (order.getUpdatedAt() != null) {
            return order.getUpdatedAt();
        }
        return order.getCreatedAt();
    }

    private String mapInternalStatusToDeliveryStatus(Order.OrderStatus status) {
        if (status == null) {
            return "unknown";
        }
        String mappedStatus;
        switch (status) {
            case PENDING:
                mappedStatus = "pending";
                break;
            case CONFIRMED:
                mappedStatus = "confirmed";
                break;
            case SHIPPED:
                mappedStatus = "in_transit";
                break;
            case DELIVERED:
                mappedStatus = "delivered";
                break;
            case CANCELLED:
                mappedStatus = "cancelled";
                break;
            default:
                mappedStatus = "unknown";
                break;
        }
        return mappedStatus.toLowerCase(Locale.ROOT);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Long userId, String status) {
        log.info("Updating order: {} status to: {} for user: {}", orderId, status, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or does not belong to user"));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            Order updatedOrder = orderRepository.save(order);
            return orderMapper.toDTO(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y \u0111\u01a1n h\u00e0ng"));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("\u0110\u01a1n h\u00e0ng \u0111\u00e3 \u0111\u01b0\u1ee3c h\u1ee7y tr\u01b0\u1edbc \u0111\u00f3");
        }
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Kh\u00f4ng th\u1ec3 h\u1ee7y \u0111\u01a1n h\u00e0ng \u0111\u00e3 giao");
        }
        if (order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Kh\u00f4ng th\u1ec3 h\u1ee7y \u0111\u01a1n h\u00e0ng \u0111ang giao");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setDeliveryStatus(null);
        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toDTO(cancelledOrder);
    }
}
