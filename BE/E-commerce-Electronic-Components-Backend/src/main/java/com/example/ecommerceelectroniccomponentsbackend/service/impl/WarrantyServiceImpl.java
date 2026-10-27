package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.warranty.*;
import com.example.ecommerceelectroniccomponentsbackend.entity.*;
import com.example.ecommerceelectroniccomponentsbackend.repository.*;
import com.example.ecommerceelectroniccomponentsbackend.service.IWarrantyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyServiceImpl implements IWarrantyService {

    private final ProductSerialRepository productSerialRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public WarrantyLookupResponseDTO lookupWarranty(String serialNumber, String phone) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Vui lòng cung cấp mã Serial để tra cứu");
        }

        String cleanSerial = serialNumber.trim().toUpperCase();
        ProductSerial serial = productSerialRepository.findBySerialNumber(cleanSerial)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bảo hành cho mã Serial: " + cleanSerial));

        // Nếu người dùng cung cấp SĐT, kiểm tra khớp
        if (phone != null && !phone.isBlank()) {
            String cleanInputPhone = phone.replaceAll("\\s+", "");
            String serialPhone = serial.getCustomerPhone() != null ? serial.getCustomerPhone().replaceAll("\\s+", "") : "";
            if (!serialPhone.isEmpty() && !serialPhone.endsWith(cleanInputPhone) && !cleanInputPhone.endsWith(serialPhone)) {
                throw new IllegalArgumentException("Số điện thoại không khớp với thông tin đăng ký của mã Serial này");
            }
        }

        boolean isExpired = serial.isExpired();
        long daysRemaining = 0;
        if (!isExpired && serial.getExpiresAt() != null) {
            daysRemaining = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), serial.getExpiresAt()));
        }

        Product product = serial.getProduct();
        ProductVariant variant = serial.getProductVariant();

        // Lấy lịch sử yêu cầu bảo hành
        List<WarrantyClaim> claims = warrantyClaimRepository.findBySerialNumberOrderByCreatedAtDesc(cleanSerial);
        List<WarrantyClaimResponseDTO> claimDTOs = claims.stream().map(this::mapClaimToDTO).toList();

        return WarrantyLookupResponseDTO.builder()
                .serialNumber(serial.getSerialNumber())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : "Linh kiện điện tử")
                .variantName(variant != null ? variant.getVariantName() : null)
                .productImageUrl(variant != null ? variant.getImageUrl() : null)
                .orderCode(serial.getOrderCode())
                .customerPhone(maskPhone(serial.getCustomerPhone()))
                .customerName(serial.getCustomerName())
                .status(isExpired ? "EXPIRED" : serial.getStatus())
                .activatedAt(serial.getActivatedAt())
                .expiresAt(serial.getExpiresAt())
                .isExpired(isExpired)
                .daysRemaining(daysRemaining)
                .warrantyMonths(product != null ? product.getWarrantyMonths() : 12)
                .claimHistory(claimDTOs)
                .build();
    }

    @Override
    @Transactional
    public WarrantyClaimResponseDTO createClaim(CreateWarrantyClaimRequestDTO request, Long userId) {
        String cleanSerial = request.getSerialNumber().trim().toUpperCase();
        ProductSerial serial = productSerialRepository.findBySerialNumber(cleanSerial)
                .orElseThrow(() -> new IllegalArgumentException("Mã Serial không tồn tại trong hệ thống: " + cleanSerial));

        if (serial.isExpired()) {
            String expDateStr = serial.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new IllegalArgumentException("Linh kiện này đã hết hạn bảo hành từ ngày " + expDateStr + ". Không thể tạo phiếu bảo hành mới.");
        }

        // Kiểm tra xem đã có phiếu đang chờ hoặc đang xử lý chưa
        List<WarrantyClaim> existingClaims = warrantyClaimRepository.findBySerialNumberOrderByCreatedAtDesc(cleanSerial);
        for (WarrantyClaim c : existingClaims) {
            if ("PENDING".equals(c.getStatus()) || "PROCESSING".equals(c.getStatus())) {
                throw new IllegalStateException("Mã Serial này đang có phiếu bảo hành mã [" + c.getClaimCode() + "] đang được xử lý. Vui lòng không tạo thêm yêu cầu trùng lặp.");
            }
        }

        // Sinh mã phiếu bảo hành duy nhất
        String claimCode = "WR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM")) + "-" + String.format("%04d", new Random().nextInt(10000));
        while (warrantyClaimRepository.findByClaimCode(claimCode).isPresent()) {
            claimCode = "WR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM")) + "-" + String.format("%04d", new Random().nextInt(10000));
        }

        WarrantyClaim claim = WarrantyClaim.builder()
                .claimCode(claimCode)
                .serial(serial)
                .serialNumber(cleanSerial)
                .userId(userId)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                .issueDescription(request.getIssueDescription().trim())
                .status("PENDING")
                .build();

        // Cập nhật trạng thái serial sang đang bảo hành
        serial.setStatus("WARRANTY_SERVICING");
        productSerialRepository.save(serial);

        WarrantyClaim savedClaim = warrantyClaimRepository.save(claim);
        log.info("Đã tạo phiếu bảo hành mới: {} cho serial: {}", claimCode, cleanSerial);

        return mapClaimToDTO(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyClaimResponseDTO getClaimByCode(String claimCode) {
        WarrantyClaim claim = warrantyClaimRepository.findByClaimCode(claimCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu bảo hành: " + claimCode));
        return mapClaimToDTO(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyClaimResponseDTO> getClaimsByUser(Long userId) {
        if (userId == null) return List.of();
        return warrantyClaimRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapClaimToDTO).toList();
    }

    @Override
    @Transactional
    public List<ProductSerial> assignSerialsToOrderItem(Long orderItemId, List<String> serialNumbers, boolean autoGenerate) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy OrderItem: " + orderItemId));

        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
        ProductVariant variant = item.getProductVariant();
        Product product = variant != null ? variant.getProduct() : null;
        int warrantyMonths = (product != null && product.getWarrantyMonths() != null) ? product.getWarrantyMonths() : 12;

        Order order = item.getOrder();
        String orderCode = order != null ? order.getOrderNumber() : "ORD-UNKNOWN";
        String customerPhone = order != null ? order.getPhone() : null;
        String customerName = order != null ? order.getFullName() : null;

        List<ProductSerial> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMonths(warrantyMonths);

        for (int i = 0; i < qty; i++) {
            String sn;
            if (serialNumbers != null && i < serialNumbers.size() && !serialNumbers.get(i).isBlank()) {
                sn = serialNumbers.get(i).trim().toUpperCase();
            } else if (autoGenerate || serialNumbers == null || serialNumbers.isEmpty()) {
                sn = "SN-" + now.format(DateTimeFormatter.ofPattern("yyMM")) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } else {
                continue;
            }

            // Nếu serial đã tồn tại thì cập nhật thông tin đơn hàng, nếu chưa thì tạo mới
            ProductSerial serial = productSerialRepository.findBySerialNumber(sn)
                    .orElse(ProductSerial.builder()
                            .serialNumber(sn)
                            .build());

            serial.setOrderItem(item);
            serial.setProduct(product);
            serial.setProductVariant(variant);
            serial.setOrderCode(orderCode);
            serial.setCustomerPhone(customerPhone);
            serial.setCustomerName(customerName);
            serial.setStatus("ACTIVE");
            serial.setActivatedAt(now);
            serial.setExpiresAt(expiresAt);

            result.add(productSerialRepository.save(serial));
        }

        log.info("Đã gán {} mã serial cho OrderItem #{}", result.size(), orderItemId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSerial> getSerialsByOrder(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) return List.of();
        return productSerialRepository.findByOrderCode(orderCode.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyClaimResponseDTO> getAllClaimsForAdmin(String status, Pageable pageable) {
        Page<WarrantyClaim> page;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            page = warrantyClaimRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase(), pageable);
        } else {
            page = warrantyClaimRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(this::mapClaimToDTO);
    }

    @Override
    @Transactional
    public WarrantyClaimResponseDTO updateClaimStatus(Long claimId, UpdateWarrantyClaimRequestDTO request) {
        WarrantyClaim claim = warrantyClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu bảo hành ID: " + claimId));

        String newStatus = request.getStatus().trim().toUpperCase();
        claim.setStatus(newStatus);
        if (request.getResolutionType() != null && !request.getResolutionType().isBlank()) {
            claim.setResolutionType(request.getResolutionType().trim().toUpperCase());
        }
        if (request.getTechnicianNotes() != null) {
            claim.setTechnicianNotes(request.getTechnicianNotes().trim());
        }

        // Cập nhật trạng thái Serial tương ứng
        ProductSerial serial = claim.getSerial();
        if (serial != null) {
            if ("COMPLETED".equals(newStatus) || "REJECTED".equals(newStatus)) {
                serial.setStatus(serial.isExpired() ? "EXPIRED" : "ACTIVE");
                productSerialRepository.save(serial);
            } else if ("PROCESSING".equals(newStatus)) {
                serial.setStatus("WARRANTY_SERVICING");
                productSerialRepository.save(serial);
            }
        }

        WarrantyClaim updated = warrantyClaimRepository.save(claim);
        log.info("Đã cập nhật phiếu bảo hành {} sang trạng thái {}", claim.getClaimCode(), newStatus);
        return mapClaimToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getWarrantyStats() {
        long totalClaims = warrantyClaimRepository.count();
        long pendingClaims = warrantyClaimRepository.countByStatus("PENDING");
        long processingClaims = warrantyClaimRepository.countByStatus("PROCESSING");
        long completedClaims = warrantyClaimRepository.countByStatus("COMPLETED");
        long rejectedClaims = warrantyClaimRepository.countByStatus("REJECTED");
        long totalSerials = productSerialRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSerials", totalSerials);
        stats.put("totalClaims", totalClaims);
        stats.put("pendingClaims", pendingClaims);
        stats.put("processingClaims", processingClaims);
        stats.put("completedClaims", completedClaims);
        stats.put("rejectedClaims", rejectedClaims);
        return stats;
    }

    private WarrantyClaimResponseDTO mapClaimToDTO(WarrantyClaim claim) {
        ProductSerial serial = claim.getSerial();
        Product product = serial != null ? serial.getProduct() : null;
        ProductVariant variant = serial != null ? serial.getProductVariant() : null;

        return WarrantyClaimResponseDTO.builder()
                .id(claim.getId())
                .claimCode(claim.getClaimCode())
                .serialNumber(claim.getSerialNumber())
                .productName(product != null ? product.getName() : "Linh kiện")
                .variantName(variant != null ? variant.getVariantName() : null)
                .userId(claim.getUserId())
                .customerName(claim.getCustomerName())
                .customerPhone(maskPhone(claim.getCustomerPhone()))
                .customerEmail(claim.getCustomerEmail())
                .issueDescription(claim.getIssueDescription())
                .status(claim.getStatus())
                .resolutionType(claim.getResolutionType())
                .technicianNotes(claim.getTechnicianNotes())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        int len = phone.length();
        return phone.substring(0, 3) + "****" + phone.substring(len - 3);
    }
}
