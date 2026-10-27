package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchKeywordStatDTO {
    private String keyword;
    private Long searchCount;
    private Integer resultsCount;
    private LocalDateTime lastSearchedAt;
}
