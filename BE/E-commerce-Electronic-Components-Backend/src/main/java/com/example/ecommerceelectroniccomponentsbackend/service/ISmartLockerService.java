package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.SmartLockerDTO;

import java.util.List;

public interface ISmartLockerService {

    List<SmartLockerDTO.Locker> getActiveLockers();

    List<SmartLockerDTO.Compartment> getAvailableCompartments(String lockerId);

    SmartLockerDTO.LockerOrderResponse createLockerOrder(SmartLockerDTO.LockerOrderRequest request);

    List<SmartLockerDTO.UserOrderResponse> getUserOrders(Long userId);
}

