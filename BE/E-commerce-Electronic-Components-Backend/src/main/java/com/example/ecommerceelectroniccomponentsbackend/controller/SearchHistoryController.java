package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.request.RecordSearchRequest;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.service.ISearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Search Analytics", description = "API thống kê và ghi nhận từ khóa tìm kiếm")
public class SearchHistoryController {

    private final ISearchHistoryService searchHistoryService;

    @PostMapping("/record")
    @Operation(summary = "Ghi nhận lịch sử tìm kiếm của người dùng")
    public ResponseEntity<Map<String, Object>> recordSearch(
            @Valid @RequestBody RecordSearchRequest request,
            Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            userId = user.getId();
        }

        searchHistoryService.recordSearch(request.getKeyword(), userId, request.getResultsCount());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã ghi nhận tìm kiếm"));
    }

    @GetMapping("/popular")
    @Operation(summary = "Lấy danh sách các từ khóa tìm kiếm phổ biến nhất cho Storefront")
    public ResponseEntity<List<SearchKeywordStatDTO>> getPopularKeywords(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(searchHistoryService.getPopularKeywords(limit));
    }
}
