package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.warranty.*;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductSerial;
import com.example.ecommerceelectroniccomponentsbackend.service.IWarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/warranties")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Warranty APIs", description = "Admin management APIs for product serials and warranty claims")
public class AdminWarrantyController {

    private final IWarrantyService warrantyService;

    @GetMapping
    @Operation(summary = "Lấy danh sách phiếu bảo hành (phân trang & lọc trạng thái)")
    public ResponseEntity<Page<WarrantyClaimResponseDTO>> getAllClaims(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<WarrantyClaimResponseDTO> res = warrantyService.getAllClaimsForAdmin(status, PageRequest.of(page, size));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats")
    @Operation(summary = "Thống kê tổng quan phiếu bảo hành và serial")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(warrantyService.getWarrantyStats());
    }

    @PutMapping("/{claimId}")
    @Operation(summary = "Tiếp nhận và cập nhật tiến độ xử lý phiếu bảo hành")
    public ResponseEntity<WarrantyClaimResponseDTO> updateClaim(
            @PathVariable("claimId") Long claimId,
            @Valid @RequestBody UpdateWarrantyClaimRequestDTO request) {
        return ResponseEntity.ok(warrantyService.updateClaimStatus(claimId, request));
    }

    @PostMapping("/orders/assign-serials")
    @Operation(summary = "Gán hoặc sinh tự động mã serial khi đóng gói linh kiện xuất kho")
    public ResponseEntity<List<ProductSerial>> assignSerials(@Valid @RequestBody AssignSerialRequestDTO request) {
        List<ProductSerial> serials = warrantyService.assignSerialsToOrderItem(
                request.getOrderItemId(),
                request.getSerialNumbers(),
                Boolean.TRUE.equals(request.getAutoGenerate())
        );
        return ResponseEntity.ok(serials);
    }
}
