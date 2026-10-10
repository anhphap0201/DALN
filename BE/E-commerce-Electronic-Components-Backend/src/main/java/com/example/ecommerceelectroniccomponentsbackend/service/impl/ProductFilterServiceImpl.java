package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductFilterRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductFilterService;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFilterServiceImpl implements IProductFilterService {

    private final ProductRepository productRepository;
    private final EntityManager entityManager;
    private final IProductSearchService productSearchService;

    @Transactional(readOnly = true)
    public Page<ProductWithVariantsDTO> filterProducts(ProductFilterRequest filter) {
        log.info("Filtering products with criteria: {}", filter);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        Join<Object, Object> variantsJoin = null;
        Join<Object, Object> categoriesJoin = null;

        // 1. Lọc theo khoảng giá (qua variants)
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            if (variantsJoin == null) {
                variantsJoin = product.join("variants", JoinType.LEFT);
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        variantsJoin.get("price"), filter.getMinPrice()
                ));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        variantsJoin.get("price"), filter.getMaxPrice()
                ));
            }
        }

        // 2. Lọc theo tình trạng kho
        if (filter.getInStock() != null && filter.getInStock()) {
            if (variantsJoin == null) {
                variantsJoin = product.join("variants", JoinType.LEFT);
            }
            predicates.add(cb.greaterThan(variantsJoin.get("inStock"), 0));
            predicates.add(cb.isTrue(variantsJoin.get("isAvailable")));
        }

        // 3. Lọc theo đánh giá tối thiểu
        if (filter.getMinRating() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    product.get("avgRating"), filter.getMinRating()
            ));
        }


        // 5. Lọc theo danh mục
        if (filter.getCategorySlug() != null) {
            if (categoriesJoin == null) {
                categoriesJoin = product.join("categories", JoinType.LEFT);
            }

            if (filter.getCategorySlug() != null) {
                predicates.add(cb.equal(
                        cb.lower(categoriesJoin.get("slug")),
                        filter.getCategorySlug().toLowerCase()
                ));
            }
        }

        // Áp dụng tất cả predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Thực thi query với phân trang
        TypedQuery<Product> typedQuery = entityManager.createQuery(query);

        // Phân trang
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<Product> products = typedQuery.getResultList();


        // Count total - rebuild predicates
        Long total = countProducts(cb, filter);

        // Convert to DTO
        List<ProductWithVariantsDTO> dtos = products.stream()
                .map(productSearchService::convertToProductWithVariantsDTO)
                .collect(Collectors.toList());

        log.info("Found {} products out of {} total", dtos.size(), total);

        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }

    private Long countProducts(CriteriaBuilder cb, ProductFilterRequest filter) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);

        List<Predicate> countPredicates = new ArrayList<>();
        Join<Object, Object> countVariantsJoin = null;
        Join<Object, Object> countCategoriesJoin = null;

        // Rebuild predicates cho count
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            countVariantsJoin = countRoot.join("variants", JoinType.LEFT);
            if (filter.getMinPrice() != null) {
                countPredicates.add(cb.greaterThanOrEqualTo(countVariantsJoin.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                countPredicates.add(cb.lessThanOrEqualTo(countVariantsJoin.get("price"), filter.getMaxPrice()));
            }
        }

        if (filter.getInStock() != null && filter.getInStock()) {
            if (countVariantsJoin == null) {
                countVariantsJoin = countRoot.join("variants", JoinType.LEFT);
            }
            countPredicates.add(cb.greaterThan(countVariantsJoin.get("inStock"), 0));
            countPredicates.add(cb.isTrue(countVariantsJoin.get("isAvailable")));
        }


        if (filter.getCategorySlug() != null) {
            countCategoriesJoin = countRoot.join("categories", JoinType.LEFT);
            if (filter.getCategorySlug() != null) {
                countPredicates.add(cb.equal(cb.lower(countCategoriesJoin.get("slug")), filter.getCategorySlug().toLowerCase()));
            }
        }

        if (filter.getMinRating() != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("avgRating"), filter.getMinRating()));
        }

        countQuery.select(cb.countDistinct(countRoot));
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    // Note: createOrder method removed - not used in current implementation
    // Sorting is handled through database query parameters

    @Transactional(readOnly = true)
    public Page<ProductWithVariantsDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
                                                           int page, int size, String sortBy) {
        log.info("Filtering products by price range: {} - {}, sortBy: {}", minPrice, maxPrice, sortBy);

        // Nếu sort theo price, dùng CriteriaQuery
        if ("price_asc".equalsIgnoreCase(sortBy) || "price_desc".equalsIgnoreCase(sortBy)) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Product> query = cb.createQuery(Product.class);
            Root<Product> product = query.from(Product.class);
            Join<Object, Object> variants = product.join("variants", JoinType.LEFT);

            // Where clause
            query.where(cb.and(
                    cb.greaterThanOrEqualTo(variants.get("price"), minPrice),
                    cb.lessThanOrEqualTo(variants.get("price"), maxPrice)
            ));

            // Order by
            if ("price_asc".equalsIgnoreCase(sortBy)) {
                query.orderBy(cb.asc(variants.get("price")));
            } else {
                query.orderBy(cb.desc(variants.get("price")));
            }

            // Execute query
            TypedQuery<Product> typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult(page * size);
            typedQuery.setMaxResults(size);

            List<Product> products = typedQuery.getResultList();

            // Remove duplicates
            products = products.stream().distinct().collect(Collectors.toList());

            // Count
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Product> countRoot = countQuery.from(Product.class);
            Join<Object, Object> countVariants = countRoot.join("variants", JoinType.LEFT);
            countQuery.select(cb.countDistinct(countRoot));
            countQuery.where(cb.and(
                    cb.greaterThanOrEqualTo(countVariants.get("price"), minPrice),
                    cb.lessThanOrEqualTo(countVariants.get("price"), maxPrice)
            ));
            Long total = entityManager.createQuery(countQuery).getSingleResult();

            // Convert to DTO
            List<ProductWithVariantsDTO> dtos = products.stream()
                    .map(productSearchService::convertToProductWithVariantsDTO)
                    .collect(Collectors.toList());

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);
        }

        // Nếu sort theo field khác, dùng repository
        Sort sort = createSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageRequest);
        return products.map(productSearchService::convertToProductWithVariantsDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductWithVariantsDTO> filterInStockProducts(int page, int size, String sortBy) {
        log.info("Filtering in-stock products");

        Sort sort = createSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findInStockProducts(pageRequest);

        return products.map(p -> productSearchService.convertToProductWithVariantsDTO(p));
    }

    @Transactional(readOnly = true)
    public Page<ProductWithVariantsDTO> filterProductsWithDiscount(int page, int size, String sortBy) {
        log.info("Filtering products with discount");

        Sort sort = createSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findProductsWithDiscount(pageRequest);

        return products.map(p -> productSearchService.convertToProductWithVariantsDTO(p));
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortBy.toLowerCase()) {
            case "price_asc", "price_desc" -> Sort.unsorted();
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "avgRating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "bestseller" -> Sort.by(Sort.Direction.DESC, "soldQuantity");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
