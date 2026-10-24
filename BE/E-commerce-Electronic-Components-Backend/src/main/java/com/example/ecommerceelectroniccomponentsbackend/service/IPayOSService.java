package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.util.Map;

public interface IPayOSService {

    Map<String, Object> createPaymentLink(Order order) throws Exception;

    Map<String, Object> processWebhook(Object webhookBody) throws Exception;

    PaymentLink getPaymentLinkInformation(Long orderCode) throws Exception;
}
