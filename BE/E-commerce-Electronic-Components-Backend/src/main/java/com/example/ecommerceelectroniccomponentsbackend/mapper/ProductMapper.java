package com.example.ecommerceelectroniccomponentsbackend.mapper;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setAvgRating(product.getAvgRating());
        dto.setSoldQuantity(product.getSoldQuantity());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setAvgRating(dto.getAvgRating());
        product.setSoldQuantity(dto.getSoldQuantity());
        return product;
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
        List<ProductDTO> list = new ArrayList<>();
        if (products != null) products.forEach(p -> list.add(toDTO(p)));
        return list;
    }

    public List<Product> toEntityList(List<ProductDTO> dtos) {
        List<Product> list = new ArrayList<>();
        if (dtos != null) dtos.forEach(d -> list.add(toEntity(d)));
        return list;
    }
}
