package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.config.PayOSConfig;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.Payment;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.PaymentRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IPayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements IPayOSService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Map<String, Object> createPaymentLink(Order order) throws Exception {
        log.info("Creating PayOS payment link for order #{}", order.getId());

        // Generate unique 11-digit numeric orderCode
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(2));

        // Build items list
        List<PaymentLinkItem> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                String itemName = "Linh kien";
                if (item.getProductVariant() != null && item.getProductVariant().getVariantName() != null) {
                    itemName = item.getProductVariant().getVariantName();
                }
                if (itemName.length() > 50) {
                    itemName = itemName.substring(0, 50);
                }
                items.add(PaymentLinkItem.builder()
                        .name(itemName)
                        .quantity(item.getQuantity())
                        .price(item.getPrice().longValue())
                        .build());
            }
        }

        // Description max 25 characters, alphanumeric & spaces
        String description = "DH " + order.getId();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        long amount = order.getTotal().longValue();

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .items(items)
                .returnUrl(payOSConfig.getReturnUrl())
                .cancelUrl(payOSConfig.getCancelUrl())
                .buyerName(order.getFullName())
                .buyerEmail(order.getEmail())
                .buyerPhone(order.getPhone())
                .buyerAddress(order.getAddress())
                .build();

        CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);

        // Record pending payment
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotal())
                .status(Payment.Status.PENDING)
                .transactionId(String.valueOf(orderCode))
                .paymentMethod("PAYOS")
                .build();
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("resultCode", 0);
        result.put("message", "success");
        result.put("checkoutUrl", response.getCheckoutUrl());
        result.put("qrCode", response.getQrCode());
        result.put("orderCode", orderCode);
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("paymentLinkId", response.getPaymentLinkId());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> processWebhook(Object webhookBody) throws Exception {
        log.info("Processing PayOS Webhook notification");
        WebhookData webhookData = payOS.webhooks().verify(webhookBody);

        Map<String, Object> response = new HashMap<>();
        if (webhookData == null) {
            log.error("PayOS Webhook verification failed or returned empty data");
            response.put("error", -1);
            response.put("message", "Invalid webhook signature");
            return response;
        }

        Long orderCode = webhookData.getOrderCode();
        log.info("PayOS Webhook received for orderCode: {}, code: {}", orderCode, webhookData.getCode());

        Payment payment = paymentRepository.findByTransactionId(String.valueOf(orderCode)).orElse(null);
        if (payment == null) {
            log.warn("PayOS Webhook: Payment record not found for transactionId (orderCode): {}", orderCode);
            response.put("error", 0);
            response.put("message", "Payment record not found, skipped");
            return response;
        }

        Order order = orderRepository.findById(payment.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("PayOS Webhook: Order not found for orderId: {}", payment.getOrderId());
            response.put("error", 0);
            response.put("message", "Order not found, skipped");
            return response;
        }

        if ("00".equals(webhookData.getCode())) {
            order.setPaymentStatus("PAID");
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            payment.setStatus(Payment.Status.SUCCESS);
            paymentRepository.save(payment);
            log.info("PayOS Webhook: Order #{} marked as PAID successfully", order.getId());
        } else {
            payment.setStatus(Payment.Status.FAILED);
            paymentRepository.save(payment);
            log.warn("PayOS Webhook: Order #{} payment failed with code {}", order.getId(), webhookData.getCode());
        }

        response.put("error", 0);
        response.put("message", "Webhook processed successfully");
        return response;
    }

    @Override
    public PaymentLink getPaymentLinkInformation(Long orderCode) throws Exception {
        return payOS.paymentRequests().get(orderCode);
    }
}
