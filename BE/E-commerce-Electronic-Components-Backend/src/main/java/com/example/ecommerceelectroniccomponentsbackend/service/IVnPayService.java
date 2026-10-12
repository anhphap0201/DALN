package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IVnPayService {

    Map<String, Object> createPaymentUrl(Order order, HttpServletRequest request) throws Exception;

    Map<String, String> processIpn(Map<String, String> params);

    Map<String, Object> processReturn(Map<String, String> params);
}
