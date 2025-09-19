package com.loopers.collector.domain.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetrics {
    private Long productId;
    private String productName;
    private LocalDate date;
    private Long viewCount;
    private Long likeCount;
    private Long orderCount;
    private BigDecimal totalOrderAmount;
    private BigDecimal rankingScore;
}
