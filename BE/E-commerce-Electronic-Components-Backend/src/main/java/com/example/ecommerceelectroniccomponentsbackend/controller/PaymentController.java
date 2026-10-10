package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.PaymentRequestDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentDTO> checkout(@Valid @RequestBody PaymentRequestDTO request) {
        PaymentDTO dto = paymentService.processPayment(getCurrentUserId(), request);
        if ("SUCCESS".equals(dto.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(dto);
    }
}
