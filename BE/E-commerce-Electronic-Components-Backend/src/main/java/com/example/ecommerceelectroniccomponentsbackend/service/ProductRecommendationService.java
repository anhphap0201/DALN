package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductRecommendationService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final IProductSearchService productSearchService;

    @Transactional(readOnly = true)
    public List<ProductWithVariantsDTO> relatedProducts(Long productId, int limit) {
        Product source = productRepository.findById(productId).orElse(null);
        if (source == null) {
            return List.of();
        }

        int resultLimit = Math.max(1, Math.min(limit, 12));
        Map<Long, ProductWithVariantsDTO> recommendations = new LinkedHashMap<>();

        orderRepository.findFrequentlyBoughtTogetherProductIds(productId, PageRequest.of(0, resultLimit))
                .forEach(id -> addAvailableProduct(recommendations, id, productId, resultLimit));

        source.getCategories().forEach(category ->
                productRepository.findByCategoryId(category.getId()).stream()
                        .filter(product -> !Objects.equals(product.getId(), productId))
                        .map(Product::getId)
                        .forEach(id -> addAvailableProduct(recommendations, id, productId, resultLimit)));

        for (String categoryKeyword : complementaryCategoryKeywords(source)) {
            productRepository.findByCategoryNameContaining(categoryKeyword).stream()
                    .map(Product::getId)
                    .forEach(id -> addAvailableProduct(recommendations, id, productId, resultLimit));
        }

        return recommendations.values().stream().limit(resultLimit).toList();
    }

    private void addAvailableProduct(Map<Long, ProductWithVariantsDTO> products, Long candidateId, Long sourceId, int limit) {
        if (products.size() >= limit || Objects.equals(candidateId, sourceId) || products.containsKey(candidateId)) {
            return;
        }
        ProductWithVariantsDTO product = productSearchService.getProductById(candidateId);
        if (product != null && product.getTotalStock() != null && product.getTotalStock() > 0) {
            products.put(candidateId, product);
        }
    }

    private List<String> complementaryCategoryKeywords(Product product) {
        String name = normalize(product.getName());
        String categories = product.getCategories().stream()
                .map(category -> normalize(category.getName()))
                .reduce("", (first, second) -> first + " " + second);

        if (name.contains("esp32") || name.contains("arduino") || categories.contains("arduino")) {
            return List.of("Cảm biến", "Module nguồn", "Motor", "Linh kiện");
        }
        if (name.contains("raspberry")) {
            return List.of("Cảm biến", "Module nguồn", "Linh kiện");
        }
        if (name.contains("cam bien") || categories.contains("cam bien")) {
            return List.of("Arduino", "Module nguồn", "Linh kiện");
        }
        if (name.contains("motor") || name.contains("servo")) {
            return List.of("Arduino", "Module nguồn");
        }
        return List.of();
    }

    private String normalize(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .toLowerCase(Locale.ROOT);
    }
}
