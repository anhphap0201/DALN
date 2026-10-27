package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.warranty.*;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductSerial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IWarrantyService {

    WarrantyLookupResponseDTO lookupWarranty(String serialNumber, String phone);

    WarrantyClaimResponseDTO createClaim(CreateWarrantyClaimRequestDTO request, Long userId);

    WarrantyClaimResponseDTO getClaimByCode(String claimCode);

    List<WarrantyClaimResponseDTO> getClaimsByUser(Long userId);

    List<ProductSerial> assignSerialsToOrderItem(Long orderItemId, List<String> serialNumbers, boolean autoGenerate);

    List<ProductSerial> getSerialsByOrder(String orderCode);

    Page<WarrantyClaimResponseDTO> getAllClaimsForAdmin(String status, Pageable pageable);

    WarrantyClaimResponseDTO updateClaimStatus(Long claimId, UpdateWarrantyClaimRequestDTO request);

    Map<String, Object> getWarrantyStats();
}
