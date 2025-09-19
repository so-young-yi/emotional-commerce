package com.loopers.interfaces.api.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingItem {
    private int rank;
    private Long productId;
    private String productName;
    private BigDecimal score;
    private LocalDate date;
}