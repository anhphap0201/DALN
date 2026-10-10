package com.example.ecommerceelectroniccomponentsbackend.mapper;

import com.example.ecommerceelectroniccomponentsbackend.dto.InventoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Inventory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryMapper {

    public InventoryDTO toDTO(Inventory inventory) {
        if (inventory == null) return null;
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct() != null ? inventory.getProduct().getId() : null);
        dto.setProductName(inventory.getProduct() != null ? inventory.getProduct().getName() : null);
        dto.setQuantity(inventory.getQuantity());
        dto.setMinQuantity(inventory.getMinQuantity());
        dto.setWarehouse(inventory.getWarehouse());
        dto.setIsLowStock(inventory.getQuantity() < inventory.getMinQuantity());
        dto.setLastUpdated(inventory.getLastUpdated());
        return dto;
    }

    public Inventory toEntity(InventoryDTO dto) {
        if (dto == null) return null;
        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQuantity(dto.getQuantity());
        inventory.setMinQuantity(dto.getMinQuantity());
        inventory.setWarehouse(dto.getWarehouse());
        return inventory;
    }

    public List<InventoryDTO> toDTOList(List<Inventory> inventories) {
        List<InventoryDTO> list = new ArrayList<>();
        if (inventories != null) inventories.forEach(i -> list.add(toDTO(i)));
        return list;
    }

    public List<Inventory> toEntityList(List<InventoryDTO> dtos) {
        List<Inventory> list = new ArrayList<>();
        if (dtos != null) dtos.forEach(d -> list.add(toEntity(d)));
        return list;
    }
}
