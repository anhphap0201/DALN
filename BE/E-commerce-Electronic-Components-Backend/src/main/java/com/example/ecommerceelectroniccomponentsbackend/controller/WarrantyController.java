package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.warranty.*;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductSerial;
import com.example.ecommerceelectroniccomponentsbackend.service.IWarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warranty")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warranty APIs", description = "Public & Customer APIs for warranty lookup and claims")
public class WarrantyController {

    private final IWarrantyService warrantyService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/lookup")
    @Operation(summary = "Tra cứu hạn bảo hành công khai bằng Mã Serial")
    public ResponseEntity<WarrantyLookupResponseDTO> lookupWarranty(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam(value = "phone", required = false) String phone) {
        WarrantyLookupResponseDTO res = warrantyService.lookupWarranty(serialNumber, phone);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/claims")
    @Operation(summary = "Tạo phiếu yêu cầu bảo hành mới")
    public ResponseEntity<WarrantyClaimResponseDTO> createClaim(@Valid @RequestBody CreateWarrantyClaimRequestDTO request) {
        Long userId = getCurrentUserId();
        WarrantyClaimResponseDTO claim = warrantyService.createClaim(request, userId);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/claims/{claimCode}")
    @Operation(summary = "Xem tiến độ phiếu bảo hành bằng mã phiếu")
    public ResponseEntity<WarrantyClaimResponseDTO> getClaimByCode(@PathVariable("claimCode") String claimCode) {
        WarrantyClaimResponseDTO claim = warrantyService.getClaimByCode(claimCode);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/my-claims")
    @Operation(summary = "Lấy danh sách phiếu bảo hành của tài khoản đăng nhập")
    public ResponseEntity<List<WarrantyClaimResponseDTO>> getMyClaims() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(warrantyService.getClaimsByUser(userId));
    }

    @GetMapping("/orders/{orderCode}/serials")
    @Operation(summary = "Lấy danh sách tem serial bảo hành thuộc đơn hàng")
    public ResponseEntity<List<ProductSerial>> getSerialsByOrder(@PathVariable("orderCode") String orderCode) {
        return ResponseEntity.ok(warrantyService.getSerialsByOrder(orderCode));
    }
}
