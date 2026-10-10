package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.InventoryDTO;

import java.util.List;
import java.util.Optional;

public interface IInventoryService {

    InventoryDTO createInventory(InventoryDTO dto);

    List<InventoryDTO> getAllInventory();

    Optional<InventoryDTO> getInventoryById(Long id);

    Optional<InventoryDTO> getInventoryByProductId(Long productId);

    List<InventoryDTO> getInventoryByWarehouse(String warehouse);

    Optional<InventoryDTO> updateInventory(Long id, InventoryDTO updatedDTO);

    Optional<InventoryDTO> updateQuantity(Long id, Integer quantity);

    boolean deleteInventory(Long id);

    boolean isLowStock(Long id);
}

