package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.config.VnPayConfig;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.Payment;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.PaymentRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IVnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayServiceImpl implements IVnPayService {

    private final VnPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Map<String, Object> createPaymentUrl(Order order, HttpServletRequest request) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = order.getOrderNumber();
        String vnp_IpAddr = getClientIp(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();

        long amount = order.getTotal().longValue() * 100;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;

        // Save a PENDING payment record
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotal())
                .status(Payment.Status.PENDING)
                .transactionId(vnp_TxnRef)
                .paymentMethod("VNPAY")
                .build();
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("resultCode", 0);
        result.put("message", "success");
        result.put("payUrl", paymentUrl);
        return result;
    }

    @Override
    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        log.info("Processing VNPAY IPN callback: {}", params);
        Map<String, String> response = new HashMap<>();

        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("vnp_")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = hashAllFields(fields);
        if (!signValue.equalsIgnoreCase(vnp_SecureHash)) {
            log.error("VNPAY IPN: Invalid Checksum");
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return response;
        }

        String orderNumber = fields.get("vnp_TxnRef");
        Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            log.error("VNPAY IPN: Order not found: {}", orderNumber);
            response.put("RspCode", "01");
            response.put("Message", "Order not Found");
            return response;
        }

        long vnpAmount = Long.parseLong(fields.get("vnp_Amount")) / 100;
        if (order.getTotal().longValue() != vnpAmount) {
            log.error("VNPAY IPN: Invalid amount. Order total: {}, VNPAY amount: {}", order.getTotal(), vnpAmount);
            response.put("RspCode", "04");
            response.put("Message", "Invalid Amount");
            return response;
        }

        if ("PAID".equals(order.getPaymentStatus())) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        String responseCode = fields.get("vnp_ResponseCode");
        String transactionStatus = fields.get("vnp_TransactionStatus");
        String vnpTranNo = fields.get("vnp_TransactionNo");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            order.setPaymentStatus("PAID");
            order.setStatus(Order.OrderStatus.CONFIRMED);
            updatePaymentRecord(order.getId(), Payment.Status.SUCCESS, vnpTranNo);
        } else {
            order.setPaymentStatus("AWAITING_PAYMENT");
            updatePaymentRecord(order.getId(), Payment.Status.FAILED, vnpTranNo);
        }
        orderRepository.save(order);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> processReturn(Map<String, String> params) {
        log.info("Processing VNPAY Return: {}", params);
        Map<String, Object> result = new HashMap<>();

        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("vnp_")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signValue = hashAllFields(fields);
        boolean signatureValid = signValue.equalsIgnoreCase(vnp_SecureHash);

        String orderNumber = fields.get("vnp_TxnRef");
        String responseCode = fields.get("vnp_ResponseCode");
        String transactionStatus = fields.get("vnp_TransactionStatus");
        String vnpTranNo = fields.get("vnp_TransactionNo");

        result.put("orderNumber", orderNumber);
        result.put("signatureValid", signatureValid);
        result.put("responseCode", responseCode);

        Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order != null) {
            result.put("orderId", order.getId());

            if (signatureValid && "00".equals(responseCode) && ("00".equals(transactionStatus) || transactionStatus == null)) {
                if (!"PAID".equals(order.getPaymentStatus())) {
                    order.setPaymentStatus("PAID");
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    updatePaymentRecord(order.getId(), Payment.Status.SUCCESS, vnpTranNo);
                }
                result.put("success", true);
                result.put("message", "Giao dịch thành công");
            } else {
                if (!"PAID".equals(order.getPaymentStatus())) {
                    order.setPaymentStatus("AWAITING_PAYMENT");
                    orderRepository.save(order);
                }
                result.put("success", false);
                result.put("message", "Giao dịch chưa hoàn tất hoặc gặp lỗi. Đơn hàng sẽ được giữ trong 24h để thanh toán lại.");
            }
        } else {
            result.put("success", false);
            result.put("message", "Đơn hàng không tồn tại");
        }

        return result;
    }

    private void updatePaymentRecord(Long orderId, Payment.Status status, String transId) {
        paymentRepository.findByOrderId(orderId).stream()
                .filter(p -> "VNPAY".equals(p.getPaymentMethod()) && p.getStatus() == Payment.Status.PENDING)
                .findFirst()
                .ifPresent(payment -> {
                    payment.setStatus(status);
                    if (transId != null) {
                        payment.setTransactionId(transId);
                    }
                    paymentRepository.save(payment);
                });
    }

    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return hmacSHA512(vnPayConfig.getHashSecret(), sb.toString());
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
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
            throw new RuntimeException("Error computing HMAC SHA-512", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return (ipAddress != null && !ipAddress.isEmpty()) ? ipAddress : "127.0.0.1";
    }
}
