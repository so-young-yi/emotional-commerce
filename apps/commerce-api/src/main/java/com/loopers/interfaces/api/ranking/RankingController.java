package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<RankingResponse> getRankings(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "1") int page) {

        log.info("Getting {} rankings for date: {}, size: {}, page: {}", period, date, size, page);

        RankingPeriod rankingPeriod = RankingPeriod.fromValue(period);
        RankingResponse response = rankingService.getRankings(rankingPeriod, date, size, page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top")
    public ResponseEntity<RankingResponse> getTopRankings(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting top {} rankings for date: {}, limit: {}", period, date, limit);

        LocalDate targetDate = date != null ? date : LocalDate.now();
        RankingPeriod rankingPeriod = RankingPeriod.fromValue(period);
        RankingResponse response = rankingService.getTopRankings(rankingPeriod, targetDate, limit);

        return ResponseEntity.ok(response);
    }
}