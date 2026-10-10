package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.AdminOrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminStatsDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.AdminUserDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.DashboardDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    // ==================== STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/chart")
    public ResponseEntity<java.util.List<DashboardDTO.DailyStats>> getChartData(
            @RequestParam(defaultValue = "30") int days) {
        if (days < 1) days = 7;
        if (days > 365) days = 365;
        return ResponseEntity.ok(adminService.getChartData(days));
    }

    // ==================== ORDERS ====================

    @GetMapping("/orders")
    public ResponseEntity<Page<AdminOrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getAllOrders(pageable, status, search));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<AdminOrderDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminService.getOrderById(orderId));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<AdminOrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateOrderStatus(orderId, status));
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        adminService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getAllUsers(pageable, search));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/toggle-active")
    public ResponseEntity<AdminUserDTO> toggleUserActive(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.toggleUserActive(userId));
    }
}
