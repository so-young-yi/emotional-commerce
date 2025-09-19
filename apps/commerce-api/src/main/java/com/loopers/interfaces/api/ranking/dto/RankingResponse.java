package com.loopers.interfaces.api.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private List<RankingItem> rankings;
    private long totalCount;
    private int currentPage;
    private int pageSize;
    private String period;
    private LocalDate date;

    public boolean hasNext() {
        return currentPage * pageSize < totalCount;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public long getTotalPages() {
        return (totalCount + pageSize - 1) / pageSize;
    }
}