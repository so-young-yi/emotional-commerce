package com.loopers.interfaces.api.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.interfaces.api.ranking.dto.RankingItem;
import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RankingService rankingService;

    @Test
    @DisplayName("일간 랭킹 조회 API 테스트")
    void getDailyRankings() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<RankingItem> mockRankings = List.of(
                new RankingItem(1, 1001L, "상품A", BigDecimal.valueOf(100.5), testDate),
                new RankingItem(2, 1002L, "상품B", BigDecimal.valueOf(85.3), testDate)
        );
        RankingResponse mockResponse = new RankingResponse(mockRankings, 2, 1, 20, "daily", testDate);

        when(rankingService.getRankings(any(RankingPeriod.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings")
                        .param("period", "daily")
                        .param("date", "2024-01-15")
                        .param("size", "20")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings").isArray())
                .andExpect(jsonPath("$.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.rankings[0].productName").value("상품A"))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.period").value("daily"));
    }

    @Test
    @DisplayName("주간 랭킹 조회 API 테스트")
    void getWeeklyRankings() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<RankingItem> mockRankings = List.of(
                new RankingItem(1, 1001L, "상품A", BigDecimal.valueOf(500.0), testDate)
        );
        RankingResponse mockResponse = new RankingResponse(mockRankings, 1, 1, 20, "weekly", testDate);

        when(rankingService.getRankings(any(RankingPeriod.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings")
                        .param("period", "weekly")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("weekly"))
                .andExpect(jsonPath("$.rankings[0].productName").value("상품A"));
    }

    @Test
    @DisplayName("TOP 랭킹 조회 API 테스트")
    void getTopRankings() throws Exception {
        // Given
        LocalDate testDate = LocalDate.now();
        List<RankingItem> mockRankings = List.of(
                new RankingItem(1, 1001L, "TOP 상품", BigDecimal.valueOf(999.9), testDate)
        );
        RankingResponse mockResponse = new RankingResponse(mockRankings, 1, 1, 10, "daily", testDate);

        when(rankingService.getTopRankings(any(RankingPeriod.class), any(LocalDate.class), anyInt()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/top")
                        .param("period", "daily")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.rankings[0].productName").value("TOP 상품"));
    }
}