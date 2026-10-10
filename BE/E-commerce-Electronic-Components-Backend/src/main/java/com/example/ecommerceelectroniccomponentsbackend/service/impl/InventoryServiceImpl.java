package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.InventoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Inventory;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.mapper.InventoryMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.InventoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryDTO createInventory(InventoryDTO dto) {
        Inventory inventory = inventoryMapper.toEntity(dto);
        if (dto.getProductId() != null) {
            Optional<Product> product = productRepository.findById(dto.getProductId());
            product.ifPresent(inventory::setProduct);
        }
        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toDTO(saved);
    }

    public List<InventoryDTO> getAllInventory() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventoryMapper.toDTOList(inventories);
    }

    public Optional<InventoryDTO> getInventoryById(Long id) {
        return inventoryRepository.findById(id).map(inventoryMapper::toDTO);
    }

    public Optional<InventoryDTO> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId).map(inventoryMapper::toDTO);
    }

    public List<InventoryDTO> getInventoryByWarehouse(String warehouse) {
        List<Inventory> inventories = inventoryRepository.findByWarehouse(warehouse);
        return inventoryMapper.toDTOList(inventories);
    }

    public Optional<InventoryDTO> updateInventory(Long id, InventoryDTO updatedDTO) {
        return inventoryRepository.findById(id).map(inventory -> {
            inventory.setQuantity(updatedDTO.getQuantity());
            inventory.setMinQuantity(updatedDTO.getMinQuantity());
            inventory.setWarehouse(updatedDTO.getWarehouse());
            Inventory saved = inventoryRepository.save(inventory);
            return inventoryMapper.toDTO(saved);
        });
    }

    public Optional<InventoryDTO> updateQuantity(Long id, Integer quantity) {
        return inventoryRepository.findById(id).map(inventory -> {
            inventory.setQuantity(quantity);
            Inventory saved = inventoryRepository.save(inventory);
            return inventoryMapper.toDTO(saved);
        });
    }

    public boolean deleteInventory(Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean isLowStock(Long id) {
        return inventoryRepository.findById(id)
                .map(inv -> inv.getQuantity() < inv.getMinQuantity())
                .orElse(false);
    }
}