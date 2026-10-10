package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentRequestDTO;

public interface IPaymentService {

    PaymentDTO processPayment(Long userId, PaymentRequestDTO request);
}

