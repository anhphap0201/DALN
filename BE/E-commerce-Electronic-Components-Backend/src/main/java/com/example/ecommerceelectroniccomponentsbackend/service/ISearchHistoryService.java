package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO;

import java.util.List;

public interface ISearchHistoryService {

    void recordSearch(String keyword, Long userId, Integer resultsCount);

    List<SearchKeywordStatDTO> getPopularKeywords(int limit);

    List<SearchKeywordStatDTO> getAdminSearchStats(int limit);

    List<SearchKeywordStatDTO> getZeroResultKeywords(int limit);
}
