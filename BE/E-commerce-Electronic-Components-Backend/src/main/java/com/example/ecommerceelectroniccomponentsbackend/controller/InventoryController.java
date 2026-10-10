package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.InventoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IInventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    @Autowired
    private IInventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInventory(@Valid @RequestBody InventoryDTO inventory) {
        try {
            InventoryDTO created = inventoryService.createInventory(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi tạo kho hàng: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventory() {
        List<InventoryDTO> inventories = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Long id) {
        Optional<InventoryDTO> inventory = inventoryService.getInventoryById(id);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Kho hàng với ID " + id + " không tồn tại");
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getInventoryByProductId(@PathVariable Long productId) {
        Optional<InventoryDTO> inventory = inventoryService.getInventoryByProductId(productId);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Kho hàng cho sản phẩm ID " + productId + " không tồn tại");
    }

    @GetMapping("/warehouse/{warehouse}")
    public ResponseEntity<?> getInventoryByWarehouse(@PathVariable String warehouse) {
        List<InventoryDTO> inventories = inventoryService.getInventoryByWarehouse(warehouse);
        if (!inventories.isEmpty()) {
            return ResponseEntity.ok(inventories);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Không có kho hàng nào với tên '" + warehouse + "'");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryDTO updatedInventory) {
        try {
            Optional<InventoryDTO> updated = inventoryService.updateInventory(id, updatedInventory);
            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Kho hàng với ID " + id + " không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật kho hàng: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Optional<InventoryDTO> updated = inventoryService.updateQuantity(id, quantity);
            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Kho hàng với ID " + id + " không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/low-stock")
    public ResponseEntity<?> isLowStock(@PathVariable Long id) {
        boolean isLow = inventoryService.isLowStock(id);
        if (isLow) {
            return ResponseEntity.ok("Cảnh báo: Số lượng hàng hóa thấp hơn mức tối thiểu");
        }
        return ResponseEntity.ok("Số lượng hàng hóa đủ");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        try {
            if (inventoryService.deleteInventory(id)) {
                return ResponseEntity.ok("Kho hàng đã được xóa thành công");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Kho hàng với ID " + id + " không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi xóa kho hàng: " + e.getMessage());
        }
    }
}
