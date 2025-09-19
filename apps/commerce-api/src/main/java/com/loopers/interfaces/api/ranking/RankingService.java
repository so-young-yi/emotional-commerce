package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ranking.dto.RankingItem;
import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    public RankingResponse getRankings(RankingPeriod period, LocalDate date, int size, int page) {
        switch (period) {
            case DAILY -> {
                return getDailyRankings(date, size, page);
            }
            case WEEKLY -> {
                return getWeeklyRankings(date, size, page);
            }
            case MONTHLY -> {
                return getMonthlyRankings(date, size, page);
            }
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }

    public RankingResponse getTopRankings(RankingPeriod period, LocalDate date, int limit) {
        return getRankings(period, date, limit, 1);
    }

    private RankingResponse getDailyRankings(LocalDate date, int size, int page) {
        String dailyKey = "ranking:daily:" + date.format(DAILY_FORMAT);

        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        Set<String> productIds = redisTemplate.opsForZSet().reverseRange(dailyKey, start, end);
        List<RankingItem> rankings = new ArrayList<>();

        if (productIds != null) {
            int rank = (int) start + 1;
            for (String productId : productIds) {
                Double score = redisTemplate.opsForZSet().score(dailyKey, productId);
                rankings.add(new RankingItem(
                    rank++,
                    Long.parseLong(productId),
                    "Product " + productId, // 실제로는 상품명을 조회해야 함
                    score != null ? BigDecimal.valueOf(score) : BigDecimal.ZERO,
                    date
                ));
            }
        }

        long totalCount = redisTemplate.opsForZSet().count(dailyKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return new RankingResponse(rankings, totalCount, page, size, RankingPeriod.DAILY.getValue(), date);
    }

    private RankingResponse getWeeklyRankings(LocalDate date, int size, int page) {
        LocalDate weekStart = getWeekStartDate(date);

        String sql = """
            SELECT product_id, product_name, ranking_score, week_start_date
            FROM mv_product_rank_weekly
            WHERE week_start_date = ?
            ORDER BY ranking_score DESC
            LIMIT ? OFFSET ?
            """;

        long offset = (long) (page - 1) * size;
        List<RankingItem> rankings = jdbcTemplate.query(sql,
            (rs, rowNum) -> new RankingItem(
                (int) offset + rowNum + 1,
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getBigDecimal("ranking_score"),
                rs.getDate("week_start_date").toLocalDate()
            ),
            weekStart, size, offset);

        String countSql = "SELECT COUNT(*) FROM mv_product_rank_weekly WHERE week_start_date = ?";
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, weekStart);

        return new RankingResponse(rankings, totalCount != null ? totalCount : 0, page, size, RankingPeriod.WEEKLY.getValue(), weekStart);
    }

    private RankingResponse getMonthlyRankings(LocalDate date, int size, int page) {
        LocalDate monthStart = date.withDayOfMonth(1);

        String sql = """
            SELECT product_id, product_name, ranking_score, month_start_date
            FROM mv_product_rank_monthly
            WHERE month_start_date = ?
            ORDER BY ranking_score DESC
            LIMIT ? OFFSET ?
            """;

        long offset = (long) (page - 1) * size;
        List<RankingItem> rankings = jdbcTemplate.query(sql,
            (rs, rowNum) -> new RankingItem(
                (int) offset + rowNum + 1,
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getBigDecimal("ranking_score"),
                rs.getDate("month_start_date").toLocalDate()
            ),
            monthStart, size, offset);

        String countSql = "SELECT COUNT(*) FROM mv_product_rank_monthly WHERE month_start_date = ?";
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, monthStart);

        return new RankingResponse(rankings, totalCount != null ? totalCount : 0, page, size, RankingPeriod.MONTHLY.getValue(), monthStart);
    }

    private LocalDate getWeekStartDate(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.with(weekFields.dayOfWeek(), 1);
    }
}