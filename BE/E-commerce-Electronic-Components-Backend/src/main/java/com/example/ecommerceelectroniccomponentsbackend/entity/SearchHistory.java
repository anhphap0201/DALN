package com.example.ecommerceelectroniccomponentsbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_histories", indexes = {
        @Index(name = "idx_search_histories_keyword", columnList = "keyword"),
        @Index(name = "idx_search_histories_created_at", columnList = "created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "results_count", nullable = false)
    @Builder.Default
    private Integer resultsCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (keyword != null) {
            keyword = keyword.trim().toLowerCase();
        }
    }
}
