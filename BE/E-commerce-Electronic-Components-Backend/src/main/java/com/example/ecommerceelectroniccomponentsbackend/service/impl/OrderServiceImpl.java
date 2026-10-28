package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.DeliveryWebhookRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.SmartLockerDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Cart;
import com.example.ecommerceelectroniccomponentsbackend.entity.CartItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.mapper.OrderMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.CartRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderMapper orderMapper;
    private final ISmartLockerService smartLockerService;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long userId, CreateOrderRequest request) {
        if (request == null) {
            request = new CreateOrderRequest();
        }

        String fullName;
        String email;
        String phone;
        String shippingAddress;
        String province;
        String ward;
        String provinceCode = request.getProvinceCode() != null ? request.getProvinceCode().trim() : "";
        String wardCode = request.getWardCode() != null ? request.getWardCode().trim() : "";

        Cart cart = null;
        List<OrderItem> itemsToOrder = new ArrayList<>();
        BigDecimal itemsSubtotal = BigDecimal.ZERO;

        if (userId != null) {
            log.info("Creating order for authenticated user: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if shipping info provided in request, otherwise fallback to user profile
            fullName = hasText(request.getFullName()) ? request.getFullName().trim() : user.getFullName();
            phone = hasText(request.getPhone()) ? safeTrim(request.getPhone()) : safeTrim(user.getPhone());
            email = hasText(request.getEmail()) ? request.getEmail().trim() : user.getEmail();
            shippingAddress = hasText(request.getAddress()) ? request.getAddress().trim() : buildShippingAddress(user);
            province = hasText(request.getProvince()) ? safeTrim(request.getProvince()) : safeTrim(user.getProvince());
            ward = hasText(request.getWard()) ? safeTrim(request.getWard()) : safeTrim(user.getWard());

            validateShippingInfo(fullName, phone, email, shippingAddress, province, ward);

            // If request has explicit items, use them; otherwise use user's DB cart
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (CreateOrderRequest.OrderItemRequest reqItem : request.getItems()) {
                    ProductVariant variant = productVariantRepository.findById(reqItem.getProductVariantId())
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm: " + reqItem.getProductVariantId()));

                    if (variant.getInStock() != null && variant.getInStock() < reqItem.getQuantity()) {
                        throw new IllegalArgumentException("Sản phẩm " + (variant.getVariantName() != null ? variant.getVariantName() : variant.getId()) + " không đủ số lượng trong kho");
                    }

                    BigDecimal itemPrice = variant.getDiscountPrice() != null ? variant.getDiscountPrice() : variant.getPrice();
                    itemsSubtotal = itemsSubtotal.add(itemPrice.multiply(BigDecimal.valueOf(reqItem.getQuantity())));

                    OrderItem orderItem = OrderItem.builder()
                            .productVariant(variant)
                            .quantity(reqItem.getQuantity())
                            .price(itemPrice)
                            .build();
                    itemsToOrder.add(orderItem);
                }
            } else {
                cart = cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

                if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                    throw new IllegalArgumentException("Cannot create order from empty cart");
                }

                for (CartItem cartItem : cart.getCartItems()) {
                    itemsSubtotal = itemsSubtotal.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                    OrderItem orderItem = OrderItem.builder()
                            .productVariant(cartItem.getProductVariant())
                            .quantity(cartItem.getQuantity())
                            .price(cartItem.getPrice())
                            .build();
                    itemsToOrder.add(orderItem);
                }
            }
        } else {
            // Guest Checkout
            log.info("Creating order for guest user");
            fullName = request.getFullName();
            phone = request.getPhone();
            email = request.getEmail();
            shippingAddress = request.getAddress();
            province = request.getProvince();
            ward = request.getWard();

            if (email == null || email.trim().isEmpty()) {
                email = safeTrim(phone) + "@guest.ecshop.vn";
            }

            validateShippingInfo(fullName, phone, email, shippingAddress, province, ward);

            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new IllegalArgumentException("Đơn hàng của khách vãng lai phải có ít nhất một sản phẩm");
            }

            for (CreateOrderRequest.OrderItemRequest reqItem : request.getItems()) {
                ProductVariant variant = productVariantRepository.findById(reqItem.getProductVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm: " + reqItem.getProductVariantId()));

                if (variant.getInStock() != null && variant.getInStock() < reqItem.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm " + (variant.getVariantName() != null ? variant.getVariantName() : variant.getId()) + " không đủ số lượng trong kho");
                }

                BigDecimal itemPrice = variant.getDiscountPrice() != null ? variant.getDiscountPrice() : variant.getPrice();
                itemsSubtotal = itemsSubtotal.add(itemPrice.multiply(BigDecimal.valueOf(reqItem.getQuantity())));

                OrderItem orderItem = OrderItem.builder()
                        .productVariant(variant)
                        .quantity(reqItem.getQuantity())
                        .price(itemPrice)
                        .build();
                itemsToOrder.add(orderItem);
            }
        }

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalTotal = itemsSubtotal.add(shippingFee).subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        // Generate unique order number
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Build order with all required fields
        Order order = Order.builder()
                .userId(userId)
                .status(Order.OrderStatus.PENDING)
                .totalCost(finalTotal)
                .totalPrice(finalTotal)
                .subtotal(itemsSubtotal)
                .total(finalTotal)
                .shippingFee(shippingFee)
                .shippingCost(shippingFee)
                .discount(discount)
                .fullName(fullName.trim())
                .email(email.trim())
                .phone(phone.trim())
                .address(shippingAddress.trim())
                .province(province.trim())
                .provinceCode(provinceCode)
                .ward(ward.trim())
                .wardCode(wardCode)
                .orderNumber(orderNumber)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .paymentStatus("PAYOS".equalsIgnoreCase(request.getPaymentMethod()) ? "UNPAID" : "PENDING")
                .shippingMethod(request.getShippingMethod() != null ? request.getShippingMethod().toUpperCase() : "STANDARD")
                .note(request.getNote())
                .voucherCode(request.getVoucherCode())
                .build();

        for (OrderItem item : itemsToOrder) {
            item.setOrder(order);
            order.getOrderItems().add(item);
        }

        // Smart Locker integration
        String shippingMethodValue = request.getShippingMethod() != null ? request.getShippingMethod().toUpperCase() : "STANDARD";
        if ("SMART_LOCKER".equals(shippingMethodValue)) {
            if (request.getLockerId() == null) {
                throw new IllegalArgumentException("Locker ID is required for smart locker delivery");
            }

            order.setLockerId(request.getLockerId());

            List<SmartLockerDTO.LockerOrderItem> lockerItems = itemsToOrder.stream()
                    .map(ci -> SmartLockerDTO.LockerOrderItem.builder()
                            .name(ci.getProductVariant() != null && ci.getProductVariant().getVariantName() != null ? ci.getProductVariant().getVariantName() : "Linh kiện")
                            .quantity(ci.getQuantity())
                            .build())
                    .toList();

            SmartLockerDTO.LockerOrderRequest lockerRequest = SmartLockerDTO.LockerOrderRequest.builder()
                    .userId(userId != null ? String.valueOf(userId) : "GUEST-" + phone.trim())
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

        // Clear cart after order creation if cart was used
        if (cart != null && cart.getCartItems() != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        return orderMapper.toDTO(savedOrder);
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private void validateShippingInfo(String fullName, String phone, String email, String address, String province, String ward) {
        if (!hasText(fullName)) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên người nhận hàng");
        }
        if (!hasText(phone)) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại người nhận");
        }
        if (!hasText(address)) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng chi tiết");
        }
        if (!hasText(province)) {
            throw new IllegalArgumentException("Vui lòng nhập Tỉnh/Thành phố");
        }
        if (!hasText(ward)) {
            throw new IllegalArgumentException("Vui lòng nhập Phường/Xã");
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

    @Override
    @Transactional
    public Page<OrderDTO> getOrderHistory(Long userId, Pageable pageable) {
        log.info("Retrieving order history for user: {}", userId);
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        LocalDateTime now = LocalDateTime.now();
        for (Order o : orders.getContent()) {
            if ("PAYOS".equalsIgnoreCase(o.getPaymentMethod())
                    && ("UNPAID".equals(o.getPaymentStatus()) || "AWAITING_PAYMENT".equals(o.getPaymentStatus()))
                    && o.getStatus() == Order.OrderStatus.PENDING) {
                if (o.getCreatedAt() != null && o.getCreatedAt().plusHours(24).isBefore(now)) {
                    o.setStatus(Order.OrderStatus.CANCELLED);
                    o.setCancelledAt(now);
                    o.setNote((o.getNote() != null ? o.getNote() + " - " : "") + "Tự động hủy do quá hạn thanh toán 24h");
                    orderRepository.save(o);
                }
            }
        }
        return orders.map(orderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, Long userId) {
        log.info("Retrieving order: {} for user: {}", orderId, userId);
        Order order;
        if (userId != null) {
            order = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found or does not belong to user"));
        } else {
            // For guest checkout: allow fetching by orderId if userId is null
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        }
        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional
    public Map<String, Object> getDeliveryStatusByReference(String orderReference) {
        log.info("Retrieving delivery status for reference: {}", orderReference);

        String normalizedReference = orderReference == null ? "" : orderReference.trim();
        if (normalizedReference.isEmpty()) {
            throw new IllegalArgumentException("order_id is required");
        }

        Order order = resolveOrderByReference(normalizedReference)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for reference: " + normalizedReference));

        Map<String, Object> deliveryStatus = new LinkedHashMap<>();
        deliveryStatus.put("order_id", order.getId());
        deliveryStatus.put("order_number", order.getOrderNumber());
        deliveryStatus.put("delivery_status", order.getDeliveryStatus());
        deliveryStatus.put("order_status", order.getStatus() != null ? order.getStatus().toString() : null);
        deliveryStatus.put("updated_at", order.getUpdatedAt());

        boolean isSmartLocker = "SMART_LOCKER".equalsIgnoreCase(order.getShippingMethod())
                || order.getLockerId() != null
                || order.getLockerOrderId() != null;

        if (isSmartLocker) {
            deliveryStatus.put("shipping_method", "SMART_LOCKER");
            deliveryStatus.put("locker_id", order.getLockerId());
            deliveryStatus.put("locker_order_id", order.getLockerOrderId());
            deliveryStatus.put("compartment_id", order.getCompartmentId());
            deliveryStatus.put("sender_otp", order.getSenderOTP());
            deliveryStatus.put("recipient_otp", order.getRecipientOTP());
        }

        return deliveryStatus;
    }

    private Optional<Order> resolveOrderByReference(String orderReference) {
        try {
            Long orderId = Long.parseLong(orderReference);
            Optional<Order> orderById = orderRepository.findById(orderId);
            if (orderById.isPresent()) {
                return orderById;
            }
        } catch (NumberFormatException ignored) {
            // Not a numeric ID, fall back to business identifiers.
        }

        return orderRepository.findByOrderNumber(orderReference)
                .or(() -> orderRepository.findByLockerOrderId(orderReference));
    }

    @Override
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

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Long userId, String status) {
        log.info("Updating status for order: {} by user: {} to: {}", orderId, userId, status);

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

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Đơn hàng đã được hủy trước đó");
        }
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng đã giao");
        }
        if (order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng đang giao");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setDeliveryStatus(null);
        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toDTO(cancelledOrder);
    }

    @Override
    @Transactional
    public OrderDTO returnOrder(Long orderId, Long userId, String reason) {
        log.info("Requesting return for order: {} by user: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Chỉ đơn hàng đã giao thành công mới được yêu cầu trả hàng");
        }

        LocalDateTime deliveredTime = order.getDeliveredAt() != null ? order.getDeliveredAt() : order.getUpdatedAt();
        if (deliveredTime != null && deliveredTime.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Đã quá thời hạn 7 ngày cho phép trả hàng");
        }

        order.setStatus(Order.OrderStatus.RETURNED);
        order.setReturnedAt(LocalDateTime.now());
        order.setReturnReason(reason != null && !reason.isBlank() ? reason.trim() : "Khách hàng yêu cầu trả hàng");
        Order returnedOrder = orderRepository.save(order);
        return orderMapper.toDTO(returnedOrder);
    }
}
