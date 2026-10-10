package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.config.MomoConfig;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.Payment;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.PaymentRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IMomoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements IMomoService {

    private final MomoConfig momoConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a MoMo payment request and return the payment URL
     */
    public Map<String, Object> createPaymentRequest(Order order) throws Exception {
        String orderId = order.getOrderNumber();
        String requestId = UUID.randomUUID().toString();
        long amount = order.getTotal().longValue();
        String orderInfo = "Thanh toan don hang " + orderId;
        String requestType = "payWithMethod";
        String extraData = "";
        boolean autoCapture = true;
        String lang = "vi";

        // Build raw signature string per MoMo API spec
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoConfig.getNotifyUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&redirectUrl=" + momoConfig.getReturnUrl()
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);
        log.debug("MoMo raw signature: {}", rawSignature);
        log.debug("MoMo signature: {}", signature);

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("partnerName", "E-commerce Electronic Components");
        requestBody.put("storeId", momoConfig.getPartnerCode());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", momoConfig.getReturnUrl());
        requestBody.put("ipnUrl", momoConfig.getNotifyUrl());
        requestBody.put("lang", lang);
        requestBody.put("requestType", requestType);
        requestBody.put("autoCapture", autoCapture);
        requestBody.put("extraData", extraData);
        requestBody.put("signature", signature);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.info("MoMo request body: {}", jsonBody);

        // Send request to MoMo
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(momoConfig.getApiUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.info("MoMo response: {}", response.body());

        JsonNode responseJson = objectMapper.readTree(response.body());

        Map<String, Object> result = new HashMap<>();
        result.put("resultCode", responseJson.get("resultCode").asInt());
        result.put("message", responseJson.get("message").asText());

        if (responseJson.has("payUrl") && !responseJson.get("payUrl").isNull()) {
            result.put("payUrl", responseJson.get("payUrl").asText());
        }
        if (responseJson.has("orderId")) {
            result.put("orderId", responseJson.get("orderId").asText());
        }
        if (responseJson.has("requestId")) {
            result.put("requestId", responseJson.get("requestId").asText());
        }

        // Save a PENDING payment record
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotal())
                .status(Payment.Status.PENDING)
                .transactionId(requestId)
                .paymentMethod("MOMO")
                .build();
        paymentRepository.save(payment);

        return result;
    }

    /**
     * Verify MoMo IPN callback signature and update order/payment status
     */
    @Transactional
    public boolean processIpnCallback(Map<String, String> params) {
        log.info("Processing MoMo IPN callback: {}", params);

        String receivedSignature = params.get("signature");
        if (receivedSignature == null) {
            log.error("MoMo IPN: missing signature");
            return false;
        }

        // Rebuild signature for verification
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + params.get("amount")
                + "&extraData=" + params.get("extraData")
                + "&message=" + params.get("message")
                + "&orderId=" + params.get("orderId")
                + "&orderInfo=" + params.get("orderInfo")
                + "&orderType=" + params.get("orderType")
                + "&partnerCode=" + params.get("partnerCode")
                + "&payType=" + params.get("payType")
                + "&requestId=" + params.get("requestId")
                + "&responseTime=" + params.get("responseTime")
                + "&resultCode=" + params.get("resultCode")
                + "&transId=" + params.get("transId");

        String expectedSignature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);

        if (!expectedSignature.equals(receivedSignature)) {
            log.error("MoMo IPN: signature mismatch. Expected: {}, Received: {}", expectedSignature, receivedSignature);
            return false;
        }

        String orderNumber = params.get("orderId");
        int resultCode = Integer.parseInt(params.get("resultCode"));
        String transId = params.get("transId");

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElse(null);

        if (order == null) {
            log.error("MoMo IPN: order not found for orderNumber: {}", orderNumber);
            return false;
        }

        // Update payment record
        paymentRepository.findByOrderId(order.getId()).stream()
                .filter(p -> "MOMO".equals(p.getPaymentMethod()) && p.getStatus() == Payment.Status.PENDING)
                .findFirst()
                .ifPresent(payment -> {
                    if (resultCode == 0) {
                        payment.setStatus(Payment.Status.SUCCESS);
                        payment.setTransactionId(transId);
                        order.setPaymentStatus("PAID");
                        order.setStatus(Order.OrderStatus.CONFIRMED);
                    } else {
                        payment.setStatus(Payment.Status.FAILED);
                        order.setPaymentStatus("FAILED");
                    }
                    paymentRepository.save(payment);
                    orderRepository.save(order);
                    log.info("MoMo IPN: Updated order {} with resultCode {}", orderNumber, resultCode);
                });

        return true;
    }

    /**
     * Process MoMo return (redirect) - verify and return status
     */
    @Transactional
    public Map<String, Object> processReturn(Map<String, String> params) {
        log.info("Processing MoMo return: {}", params);

        Map<String, Object> result = new HashMap<>();
        String orderNumber = params.get("orderId");
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));

        result.put("orderNumber", orderNumber);
        result.put("resultCode", resultCode);
        result.put("message", params.getOrDefault("message", ""));

        Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order != null) {
            result.put("orderId", order.getId());

            // Verify signature
            String receivedSignature = params.get("signature");
            String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                    + "&amount=" + params.get("amount")
                    + "&extraData=" + params.get("extraData")
                    + "&message=" + params.get("message")
                    + "&orderId=" + params.get("orderId")
                    + "&orderInfo=" + params.get("orderInfo")
                    + "&orderType=" + params.get("orderType")
                    + "&partnerCode=" + params.get("partnerCode")
                    + "&payType=" + params.get("payType")
                    + "&requestId=" + params.get("requestId")
                    + "&responseTime=" + params.get("responseTime")
                    + "&resultCode=" + params.get("resultCode")
                    + "&transId=" + params.get("transId");

            String expectedSignature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);
            boolean signatureValid = expectedSignature.equals(receivedSignature);
            result.put("signatureValid", signatureValid);

            if (signatureValid && resultCode == 0) {
                // Update order if IPN hasn't already done it
                if (!"PAID".equals(order.getPaymentStatus())) {
                    String transId = params.get("transId");
                    order.setPaymentStatus("PAID");
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);

                    paymentRepository.findByOrderId(order.getId()).stream()
                            .filter(p -> "MOMO".equals(p.getPaymentMethod()) && p.getStatus() == Payment.Status.PENDING)
                            .findFirst()
                            .ifPresent(payment -> {
                                payment.setStatus(Payment.Status.SUCCESS);
                                payment.setTransactionId(transId);
                                paymentRepository.save(payment);
                            });
                }
                result.put("success", true);
            } else {
                result.put("success", false);
            }
        } else {
            result.put("success", false);
        }

        return result;
    }

    /**
     * HMAC SHA-256 signing
     */
    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC SHA-256", e);
        }
    }
}
