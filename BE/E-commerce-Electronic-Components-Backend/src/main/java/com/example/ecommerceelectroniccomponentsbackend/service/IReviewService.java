package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateReviewRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ReviewSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviewService {

    Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable);

    ReviewSummaryDTO getReviewSummary(Long productId);

    ReviewDTO createReview(Long userId, CreateReviewRequest request);

    ReviewDTO updateReview(Long userId, Long reviewId, CreateReviewRequest request);

    void deleteReview(Long userId, Long reviewId);
}

