package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("SELECT new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO(" +
            "s.keyword, " +
            "COUNT(s.id), " +
            "MAX(s.resultsCount), " +
            "MAX(s.createdAt)) " +
            "FROM SearchHistory s " +
            "GROUP BY s.keyword " +
            "ORDER BY COUNT(s.id) DESC")
    List<SearchKeywordStatDTO> findTopPopularKeywords(Pageable pageable);

    @Query("SELECT new com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO(" +
            "s.keyword, " +
            "COUNT(s.id), " +
            "0, " +
            "MAX(s.createdAt)) " +
            "FROM SearchHistory s " +
            "WHERE s.resultsCount = 0 " +
            "GROUP BY s.keyword " +
            "ORDER BY COUNT(s.id) DESC")
    List<SearchKeywordStatDTO> findTopZeroResultKeywords(Pageable pageable);

    long countByKeywordIgnoreCase(String keyword);
}
