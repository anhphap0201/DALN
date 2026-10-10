package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateReviewRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewSummaryDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.Review;
import com.example.ecommerceelectroniccomponentsbackend.model.User;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ReviewRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.UserRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDTO getReviewSummary(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        long totalReviews = reviewRepository.countByProductId(productId);

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distribution.put(star, reviewRepository.countByProductIdAndRating(productId, star));
        }

        return ReviewSummaryDTO.builder()
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews)
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional
    public ReviewDTO createReview(Long userId, CreateReviewRequest request) {
        log.info("Creating review for product {} by user {}", request.getProductId(), userId);

        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        // Update product avg rating
        updateProductAvgRating(product.getId());

        return toDTO(saved);
    }

    @Transactional
    public ReviewDTO updateReview(Long userId, Long reviewId, CreateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (review.getUser().getId() != userId) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        // Update product avg rating
        updateProductAvgRating(review.getProduct().getId());

        return toDTO(saved);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (review.getUser().getId() != userId) {
            throw new IllegalArgumentException("Bạn không có quyền xóa đánh giá này");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        // Update product avg rating
        updateProductAvgRating(productId);
    }

    private void updateProductAvgRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setAvgRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            productRepository.save(product);
        }
    }

    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : "Người dùng")
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
