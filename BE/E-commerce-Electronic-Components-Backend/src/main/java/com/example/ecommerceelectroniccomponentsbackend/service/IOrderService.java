package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.DeliveryWebhookRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IOrderService {

    OrderDTO createOrderFromCart(Long userId, CreateOrderRequest request);

    Page<OrderDTO> getOrderHistory(Long userId, Pageable pageable);

    OrderDTO getOrderById(Long orderId, Long userId);

    Map<String, Object> getDeliveryStatusByReference(String orderReference);

    OrderDTO updateOrderStatus(Long orderId, Long userId, String status);

    OrderDTO cancelOrder(Long orderId, Long userId);

    Map<String, Object> handleDeliveryWebhook(DeliveryWebhookRequest request);
}

