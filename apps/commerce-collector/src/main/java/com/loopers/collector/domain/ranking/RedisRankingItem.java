package com.loopers.collector.domain.ranking;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RedisRankingItem {
    private Long productId;
    private String productName;
    private LocalDate date;
    private BigDecimal dailyScore;
    private BigDecimal weeklyScore;
    private BigDecimal monthlyScore;

    public void setWeeklyScore(BigDecimal weeklyScore) {
        this.weeklyScore = weeklyScore;
    }

    public void setMonthlyScore(BigDecimal monthlyScore) {
        this.monthlyScore = monthlyScore;
    }
}
