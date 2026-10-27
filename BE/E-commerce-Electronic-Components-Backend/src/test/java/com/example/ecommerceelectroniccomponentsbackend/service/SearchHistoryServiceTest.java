package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.SearchKeywordStatDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.SearchHistory;
import com.example.ecommerceelectroniccomponentsbackend.repository.SearchHistoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.impl.SearchHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    private ISearchHistoryService searchHistoryService;

    @BeforeEach
    void setUp() {
        searchHistoryService = new SearchHistoryServiceImpl(searchHistoryRepository);
    }

    @Test
    @DisplayName("Ghi nhận lịch sử tìm kiếm: Chuẩn hóa chữ thường, cắt khoảng trắng và lưu DB")
    void testRecordSearch_Success() {
        searchHistoryService.recordSearch("  ESP32 DevKit V1  ", 10L, 5);

        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository, times(1)).save(captor.capture());

        SearchHistory saved = captor.getValue();
        assertEquals("esp32 devkit v1", saved.getKeyword());
        assertEquals(10L, saved.getUserId());
        assertEquals(5, saved.getResultsCount());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Ghi nhận lịch sử tìm kiếm: Bỏ qua chuỗi rỗng hoặc quá ngắn (< 2 ký tự)")
    void testRecordSearch_IgnoreTooShort() {
        searchHistoryService.recordSearch(" a ", null, 0);
        searchHistoryService.recordSearch(null, null, 0);
        searchHistoryService.recordSearch("   ", null, 0);

        verify(searchHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lấy danh sách từ khóa phổ biến cho Storefront")
    void testGetPopularKeywords() {
        when(searchHistoryRepository.findTopPopularKeywords(PageRequest.of(0, 8)))
                .thenReturn(List.of(
                        new SearchKeywordStatDTO("arduino uno", 50L, 10, LocalDateTime.now()),
                        new SearchKeywordStatDTO("esp32", 30L, 8, LocalDateTime.now())
                ));

        List<SearchKeywordStatDTO> list = searchHistoryService.getPopularKeywords(8);
        assertEquals(2, list.size());
        assertEquals("arduino uno", list.get(0).getKeyword());
        assertEquals(50L, list.get(0).getSearchCount());
    }

    @Test
    @DisplayName("Lấy danh sách thống kê từ khóa cho Admin Dashboard")
    void testGetAdminSearchStats() {
        when(searchHistoryRepository.findTopPopularKeywords(PageRequest.of(0, 20)))
                .thenReturn(List.of(
                        new SearchKeywordStatDTO("dong co servo", 25L, 4, LocalDateTime.now())
                ));

        List<SearchKeywordStatDTO> list = searchHistoryService.getAdminSearchStats(20);
        assertEquals(1, list.size());
        assertEquals("dong co servo", list.get(0).getKeyword());
    }
}
