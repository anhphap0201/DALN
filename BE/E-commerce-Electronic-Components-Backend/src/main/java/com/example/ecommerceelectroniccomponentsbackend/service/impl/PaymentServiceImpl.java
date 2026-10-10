package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.Payment;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.PaymentRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentDTO processPayment(Long userId, PaymentRequestDTO request) {
        log.info("Processing payment for user: {} order: {}", userId, request.getOrderId());

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or does not belong to user"));

        // simple mock gateway: accept if amounts match
        Payment.Status status = Payment.Status.FAILED;
        if (order.getTotalPrice().compareTo(request.getAmount()) == 0) {
            status = Payment.Status.SUCCESS;
        }

        String txId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(request.getAmount())
                .status(status)
                .transactionId(txId)
                .build();

        Payment saved = paymentRepository.save(payment);

        if (status == Payment.Status.SUCCESS) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return PaymentDTO.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .amount(saved.getAmount())
                .status(saved.getStatus().name())
                .transactionId(saved.getTransactionId())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
