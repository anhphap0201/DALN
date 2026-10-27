package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.SearchHistory;
import com.example.ecommerceelectroniccomponentsbackend.repository.SearchHistoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.ISearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryServiceImpl implements ISearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    @Override
    @Transactional
    public void recordSearch(String keyword, Long userId, Integer resultsCount) {
        if (keyword == null) return;
        String cleaned = keyword.trim().toLowerCase();
        if (cleaned.length() < 2 || cleaned.length() > 255) {
            return;
        }

        try {
            SearchHistory history = SearchHistory.builder()
                    .keyword(cleaned)
                    .userId(userId)
                    .resultsCount(resultsCount != null ? Math.max(0, resultsCount) : 0)
                    .createdAt(LocalDateTime.now())
                    .build();
            searchHistoryRepository.save(history);
            log.debug("Recorded search keyword: '{}' (results: {})", cleaned, resultsCount);
        } catch (Exception e) {
            log.warn("Failed to record search history: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeywordStatDTO> getPopularKeywords(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return searchHistoryRepository.findTopPopularKeywords(PageRequest.of(0, safeLimit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeywordStatDTO> getAdminSearchStats(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return searchHistoryRepository.findTopPopularKeywords(PageRequest.of(0, safeLimit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeywordStatDTO> getZeroResultKeywords(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return searchHistoryRepository.findTopZeroResultKeywords(PageRequest.of(0, safeLimit));
    }
}
