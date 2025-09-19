package com.loopers.collector.application.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

    @Qualifier("redisTemplateMaster")
    private final RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter WEEKLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-ww");
    private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    // 가중치
    private static final double VIEW_WEIGHT = 0.1;
    private static final double LIKE_WEIGHT = 0.2;
    private static final double ORDER_WEIGHT = 1.0;

    // TTL (일)
    private static final int DAILY_TTL_DAYS = 8;
    private static final int WEEKLY_TTL_DAYS = 35;
    private static final int MONTHLY_TTL_DAYS = 400;

    public void updateProductView(String productId) {
        updateRanking(productId, VIEW_WEIGHT, "VIEW");
    }

    public void updateProductLike(String productId) {
        updateRanking(productId, LIKE_WEIGHT, "LIKE");
    }

    public void updateProductOrder(String productId, int quantity, long price) {
        // 주문은 수량 * 가격을 기반으로 점수 계산 (log 정규화)
        double score = ORDER_WEIGHT * Math.log1p(quantity * price);
        updateRanking(productId, score, "ORDER");
    }

    private void updateRanking(String productId, double score, String eventType) {
        LocalDate today = LocalDate.now();
        
        String dailyKey = getDailyRankingKey(today);
        String weeklyKey = getWeeklyRankingKey(today);
        
        try {
            // Redis Pipeline 사용하여 배치 업데이트
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                // 일간 랭킹 업데이트
                connection.zIncrBy(dailyKey.getBytes(), score, productId.getBytes());
                connection.expire(dailyKey.getBytes(), TimeUnit.DAYS.toSeconds(DAILY_TTL_DAYS));
                
                // 주간 랭킹 업데이트
                connection.zIncrBy(weeklyKey.getBytes(), score, productId.getBytes());
                connection.expire(weeklyKey.getBytes(), TimeUnit.DAYS.toSeconds(WEEKLY_TTL_DAYS));
                
                return null;
            });
            
            log.debug("Updated ranking for product: {} with score: {} for event: {}", productId, score, eventType);
            
        } catch (Exception e) {
            log.error("Failed to update ranking for product: {}, event: {}", productId, eventType, e);
        }
    }

    public Set<String> getDailyTopProducts(int limit) {
        return getTopProducts(getDailyRankingKey(LocalDate.now()), limit);
    }

    public Set<String> getWeeklyTopProducts(int limit) {
        return getTopProducts(getWeeklyRankingKey(LocalDate.now()), limit);
    }

    private Set<String> getTopProducts(String key, int limit) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        } catch (Exception e) {
            log.error("Failed to get top products from key: {}", key, e);
            return Set.of();
        }
    }

    private String getDailyRankingKey(LocalDate date) {
        return "ranking:daily:" + date.format(DAILY_FORMAT);
    }

    private String getWeeklyRankingKey(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfYear());
        return String.format("ranking:weekly:%d-%02d", year, week);
    }

    private String getMonthlyRankingKey(LocalDate date) {
        return "ranking:monthly:" + date.format(MONTHLY_FORMAT);
    }
}
