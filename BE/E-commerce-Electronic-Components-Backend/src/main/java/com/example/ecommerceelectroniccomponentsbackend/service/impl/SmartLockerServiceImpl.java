package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.SmartLockerDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.ISmartLockerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartLockerServiceImpl implements ISmartLockerService {

    private final RestClient smartLockerRestClient;

    /**
     * Get all active lockers
     */
    public List<SmartLockerDTO.Locker> getActiveLockers() {
        log.info("Fetching active lockers from smart locker API");
        return smartLockerRestClient.get()
                .uri("/lockers/")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Get available compartments for a locker
     */
    public List<SmartLockerDTO.Compartment> getAvailableCompartments(String lockerId) {
        log.info("Fetching available compartments for locker: {}", lockerId);
        return smartLockerRestClient.get()
                .uri("/compartments/locker/{lockerId}/available", lockerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Create a locker order to reserve a compartment
     */
    public SmartLockerDTO.LockerOrderResponse createLockerOrder(SmartLockerDTO.LockerOrderRequest request) {
        log.info("Creating locker order for locker: {}, user: {}",
                request.getLockerId(), request.getUserId());
        try {
            return smartLockerRestClient.post()
                    .uri("/orders/")
                    .body(request)
                    .retrieve()
                    .body(SmartLockerDTO.LockerOrderResponse.class);
        } catch (RestClientResponseException e) {
            String detail = extractErrorDetail(e);
            log.error("Smart locker API error: {}", detail);
            throw new IllegalStateException(detail, e);
        }
    }

    /**
     * Get all user orders with delivery status and OTP from smart locker
     */
    public List<SmartLockerDTO.UserOrderResponse> getUserOrders(Long userId) {
        log.info("Fetching user orders from smart locker for user: {}", userId);
        try {
            return smartLockerRestClient.get()
                    .uri("/orders/user/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException e) {
            String detail = extractErrorDetail(e);
            log.error("Failed to fetch user orders for user {}: {}", userId, detail);
            throw new IllegalStateException(detail, e);
        }
    }

    private String extractErrorDetail(RestClientResponseException e) {
        try {
            String body = e.getResponseBodyAsString();
            JsonNode node = new ObjectMapper().readTree(body);
            if (node.has("detail")) {
                return node.get("detail").asText();
            }
            return body;
        } catch (Exception ex) {
            return e.getMessage();
        }
    }
}
