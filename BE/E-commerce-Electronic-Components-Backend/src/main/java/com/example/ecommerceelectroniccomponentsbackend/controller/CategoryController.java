package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO category) {
        try {
            if (categoryService.existsByName(category.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Danh mục với tên này đã tồn tại");
            }
            CategoryDTO created = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi tạo danh mục: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Optional<CategoryDTO> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Danh mục với ID " + id + " không tồn tại");
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<?> getCategoryByName(@PathVariable String name) {
        Optional<CategoryDTO> category = categoryService.getCategoryByName(name);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Danh mục với tên '" + name + "' không tồn tại");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO updatedCategory) {
        try {
            Optional<CategoryDTO> updated = categoryService.updateCategory(id, updatedCategory);
            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Danh mục với ID " + id + " không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật danh mục: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            if (categoryService.deleteCategory(id)) {
                return ResponseEntity.ok("Danh mục đã được xóa thành công");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Danh mục với ID " + id + " không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi xóa danh mục: " + e.getMessage());
        }
    }
}
