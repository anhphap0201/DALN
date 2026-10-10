package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.mapper.ProductMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = productMapper.toEntity(dto);
        Product saved = productRepository.saveAndFlush(product);
        return productMapper.toDTO(saved);
    }

    public List<ProductDTO> findAll() {
        List<Product> products = productRepository.findAll();
        return productMapper.toDTOList(products);
    }

    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id).map(productMapper::toDTO);
    }

    public ProductDTO updateProductById(Long id, ProductDTO updatedDTO) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedDTO.getName());
            product.setSlug(updatedDTO.getSlug());
            product.setAvgRating(updatedDTO.getAvgRating());
            product.setSoldQuantity(updatedDTO.getSoldQuantity());
            Product saved = productRepository.save(product);
            return productMapper.toDTO(saved);
        }
        return null;
    }

    public boolean deleteProductById(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<ProductDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toDTOList(products);
    }

    public List<ProductDTO> findByCategoryName(String categoryName) {
        List<Product> products = productRepository.findByCategoryNameContaining(categoryName);
        return productMapper.toDTOList(products);
    }

    public List<ProductDTO> findByCategoryId(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return productMapper.toDTOList(products);
    }

    public List<ProductDTO> findByCategorySlug(String slug) {
        List<Product> products = productRepository.findByCategorySlug(slug);
        return productMapper.toDTOList(products);
    }

    public Page<ProductDTO> findByMinRating(Double minRating, Pageable pageable) {
        Page<Product> products = productRepository.findByMinRating(minRating, pageable);
        return products.map(productMapper::toDTO);
    }

    public Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return products.map(productMapper::toDTO);
    }

    public Page<ProductDTO> findInStockProducts(Pageable pageable) {
        Page<Product> products = productRepository.findInStockProducts(pageable);
        return products.map(productMapper::toDTO);
    }

    public Page<ProductDTO> findProductsWithDiscount(Pageable pageable) {
        Page<Product> products = productRepository.findProductsWithDiscount(pageable);
        return products.map(productMapper::toDTO);
    }
}
