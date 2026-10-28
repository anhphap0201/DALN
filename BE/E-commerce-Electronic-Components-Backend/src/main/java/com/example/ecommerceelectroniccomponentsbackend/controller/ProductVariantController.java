package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductVariantAdminDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/product-variants")
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    // GET API → Fetch all variants for admin
    @GetMapping
    public List<ProductVariantAdminDTO> getAllVariants() {
        return productVariantService.getAllVariantsForAdmin();
    }

    // POST API → Add new variant
    @PostMapping
    public ProductVariant addVariant(@RequestBody ProductVariant variant) {
        return productVariantService.addVariant(variant);
    }

    // PUT API → Update variant by ID
    @PutMapping("/{id}")
    public ProductVariant updateVariant(@PathVariable Long id, @RequestBody ProductVariant updatedVariant) {
        return productVariantService.updateVariantById(id, updatedVariant);
    }

    // DELETE API → Remove variant by ID
    @DeleteMapping("/{id}")
    public String deleteVariant(@PathVariable Long id) {
        productVariantService.deleteVariantById(id);
        return "Product variant with id: " + id + " has been deleted";
    }

    public static record DiscountUpdateRequest(java.math.BigDecimal discountPrice) {}
    public static record BatchDiscountRequest(List<Long> variantIds, Integer discountPercent) {}

    // PATCH /product-variants/{id}/discount
    @PatchMapping("/{id}/discount")
    public org.springframework.http.ResponseEntity<ProductVariantAdminDTO> updateDiscount(
            @PathVariable Long id,
            @RequestBody DiscountUpdateRequest request
    ) {
        ProductVariantAdminDTO updated = productVariantService.updateDiscount(id, request != null ? request.discountPrice() : null);
        return org.springframework.http.ResponseEntity.ok(updated);
    }

    // POST /product-variants/batch-discount
    @PostMapping("/batch-discount")
    public org.springframework.http.ResponseEntity<List<ProductVariantAdminDTO>> batchDiscount(
            @RequestBody BatchDiscountRequest request
    ) {
        List<ProductVariantAdminDTO> updated = productVariantService.updateBatchDiscount(
                request != null ? request.variantIds() : null,
                request != null ? request.discountPercent() : null
        );
        return org.springframework.http.ResponseEntity.ok(updated);
    }
}
