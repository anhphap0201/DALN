package com.example.ecommerceelectroniccomponentsbackend.service;


import com.example.ecommerceelectroniccomponentsbackend.entity.Order;

import java.util.Map;

public interface IMomoService {

    Map<String, Object> createPaymentRequest(Order order) throws Exception;

    boolean processIpnCallback(Map<String, String> params);

    Map<String, Object> processReturn(Map<String, String> params);
}

