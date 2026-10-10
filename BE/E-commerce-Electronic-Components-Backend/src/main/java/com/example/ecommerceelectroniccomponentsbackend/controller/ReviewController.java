package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateReviewRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewSummaryDTO;
import com.example.ecommerceelectroniccomponentsbackend.service.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    /**
     * Get reviews for a product (public)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId, pageable));
    }

    /**
     * Get review summary (avg rating + distribution) for a product (public)
     */
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ReviewSummaryDTO> getReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(productId));
    }

    /**
     * Create a new review (authenticated)
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO review = reviewService.createReview(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * Update an existing review (authenticated, owner only)
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO review = reviewService.updateReview(getCurrentUserId(), reviewId, request);
        return ResponseEntity.ok(review);
    }

    /**
     * Delete a review (authenticated, owner only)
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(getCurrentUserId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
