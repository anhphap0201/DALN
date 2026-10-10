package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductFilterRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductFilterService;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Product Search", description = "API tìm kiếm sản phẩm")
public class ProductSearchController {

    private final IProductSearchService productSearchService;
    private final IProductFilterService productFilterService;

    @GetMapping("/by-category-name")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên danh mục",
            description = "Tìm tất cả sản phẩm (kèm variants) thuộc danh mục có tên chứa từ khóa tìm kiếm")
    public ResponseEntity<List<ProductWithVariantsDTO>> searchByCategoryName(
            @Parameter(description = "Tên danh mục cần tìm", example = "Arduino")
            @RequestParam String categoryName) {
        List<ProductWithVariantsDTO> products = productSearchService.searchProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-category-id/{categoryId}")
    @Operation(summary = "Tìm kiếm sản phẩm theo ID danh mục",
            description = "Tìm tất cả sản phẩm (kèm variants) thuộc danh mục theo ID")
    public ResponseEntity<List<ProductWithVariantsDTO>> searchByCategoryId(
            @Parameter(description = "ID của danh mục", example = "1")
            @PathVariable Long categoryId) {
        List<ProductWithVariantsDTO> products = productSearchService.searchProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-category-slug")
    @Operation(summary = "Tìm kiếm sản phẩm theo slug danh mục",
            description = "Tìm tất cả sản phẩm (kèm variants) thuộc danh mục theo slug")
    public ResponseEntity<List<ProductWithVariantsDTO>> searchByCategorySlug(
            @Parameter(description = "Slug của danh mục", example = "arduino")
            @RequestParam String categorySlug) {
        List<ProductWithVariantsDTO> products = productSearchService.searchProductsByCategorySlug(categorySlug);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-product-name")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên sản phẩm",
            description = "Tìm sản phẩm (kèm variants) có tên chứa từ khóa tìm kiếm")
    public ResponseEntity<List<ProductWithVariantsDTO>> searchByProductName(
            @Parameter(description = "Tên sản phẩm cần tìm", example = "Arduino Uno")
            @RequestParam String productName) {
        List<ProductWithVariantsDTO> products = productSearchService.searchProductsByName(productName);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-product-id/{productId}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID",
            description = "Lấy sản phẩm (kèm variants) theo ID sản phẩm")
    public ResponseEntity<ProductWithVariantsDTO> getProductById(
            @Parameter(description = "ID của sản phẩm", example = "1")
            @PathVariable Long productId) {
        ProductWithVariantsDTO product = productSearchService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }


    @PostMapping("/filter")
    @Operation(summary = "Lọc sản phẩm theo nhiều tiêu chí",
            description = "Lọc sản phẩm theo: khoảng giá (minPrice, maxPrice), tình trạng kho (inStock), đánh giá tối thiểu (minRating), danh mục (categoryId hoặc categorySlug)")
    public ResponseEntity<Page<ProductWithVariantsDTO>> filterProducts(
            @RequestBody ProductFilterRequest filter) {
        Page<ProductWithVariantsDTO> products = productFilterService.filterProducts(filter);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/by-price")
    @Operation(summary = "Lọc sản phẩm theo khoảng giá",
            description = "Lọc sản phẩm có giá trong khoảng từ minPrice đến maxPrice")
    public ResponseEntity<Page<ProductWithVariantsDTO>> filterByPrice(
            @Parameter(description = "Giá tối thiểu", example = "100000")
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Giá tối đa", example = "500000")
            @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng sản phẩm mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo: name_asc, name_desc, rating_desc, newest, bestseller")
            @RequestParam(defaultValue = "newest") String sortBy) {
        Page<ProductWithVariantsDTO> products = productFilterService.filterByPriceRange(
                minPrice, maxPrice, page, size, sortBy
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/in-stock")
    @Operation(summary = "Lọc sản phẩm còn hàng",
            description = "Lấy danh sách sản phẩm còn hàng trong kho")
    public ResponseEntity<Page<ProductWithVariantsDTO>> filterInStock(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng sản phẩm mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo: name_asc, name_desc, rating_desc, newest, bestseller")
            @RequestParam(defaultValue = "newest") String sortBy) {
        Page<ProductWithVariantsDTO> products = productFilterService.filterInStockProducts(page, size, sortBy);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/on-sale")
    @Operation(summary = "Lọc sản phẩm đang giảm giá",
            description = "Lấy danh sách sản phẩm đang có giảm giá")
    public ResponseEntity<Page<ProductWithVariantsDTO>> filterOnSale(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng sản phẩm mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo: name_asc, name_desc, rating_desc, newest, bestseller")
            @RequestParam(defaultValue = "newest") String sortBy) {
        Page<ProductWithVariantsDTO> products = productFilterService.filterProductsWithDiscount(page, size, sortBy);
        return ResponseEntity.ok(products);
    }
}
