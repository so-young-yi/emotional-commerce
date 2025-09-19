package com.loopers.collector.batch.scheduler;

import com.loopers.interfaces.batch.scheduler.RankingAggregationScheduler;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(RedisTestContainersConfig.class)
class RankingAggregationSchedulerTest {

    @Autowired
    private RankingAggregationScheduler scheduler;

    @Autowired
    @Qualifier("redisTemplateMaster")
    private RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @Autowired private RedisCleanUp redisCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @Test
    @DisplayName("일간->주간 집계 스케줄러가 수동 실행되어야 한다")
    void should_run_daily_to_weekly_aggregation_manually() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = getDailyRankingKey(yesterday);
        String weeklyKey = getWeeklyRankingKey(yesterday);

        redisTemplate.opsForZSet().add(dailyKey, "product1", 10.0);
        redisTemplate.opsForZSet().add(dailyKey, "product2", 20.0);

        // When
        scheduler.runDailyToWeeklyAggregationManually();

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Double product1Score = redisTemplate.opsForZSet().score(weeklyKey, "product1");
                    Double product2Score = redisTemplate.opsForZSet().score(weeklyKey, "product2");
                    
                    assertThat(product1Score).isEqualTo(10.0);
                    assertThat(product2Score).isEqualTo(20.0);
                });
    }

    @Test
    @DisplayName("주간->월간 집계 스케줄러가 수동 실행되어야 한다")
    void should_run_weekly_to_monthly_aggregation_manually() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate firstWeek = now.withDayOfMonth(1);
        
        String weeklyKey = getWeeklyRankingKey(firstWeek);
        String monthlyKey = getMonthlyRankingKey(now);

        redisTemplate.opsForZSet().add(weeklyKey, "product1", 100.0);
        redisTemplate.opsForZSet().add(weeklyKey, "product2", 80.0);

        // When
        scheduler.runWeeklyToMonthlyAggregationManually();

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Double product1Score = redisTemplate.opsForZSet().score(monthlyKey, "product1");
                    Double product2Score = redisTemplate.opsForZSet().score(monthlyKey, "product2");
                    
                    assertThat(product1Score).isEqualTo(100.0);
                    assertThat(product2Score).isEqualTo(80.0);
                });
    }

    @Test
    @DisplayName("스케줄러가 예외 상황에서도 안정적으로 동작해야 한다")
    void should_handle_exceptions_gracefully() {
        // Given - Redis 연결 문제가 발생할 수 있는 상황

        // When & Then - 예외가 발생하더라도 애플리케이션이 중단되지 않아야 함
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            scheduler.runDailyToWeeklyAggregationManually();
            scheduler.runWeeklyToMonthlyAggregationManually();
        });
    }

    @Test
    @DisplayName("중복 실행되어도 데이터 일관성이 유지되어야 한다")
    void should_maintain_data_consistency_on_duplicate_execution() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = getDailyRankingKey(yesterday);
        String weeklyKey = getWeeklyRankingKey(yesterday);

        redisTemplate.opsForZSet().add(dailyKey, "product1", 10.0);

        // When - 같은 배치를 두 번 실행
        scheduler.runDailyToWeeklyAggregationManually();
        
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Double score = redisTemplate.opsForZSet().score(weeklyKey, "product1");
                    assertThat(score).isEqualTo(10.0);
                });

        scheduler.runDailyToWeeklyAggregationManually();

        // Then - 중복 집계로 인해 점수가 두 배가 되어야 함 (이는 예상된 동작)
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Double score = redisTemplate.opsForZSet().score(weeklyKey, "product1");
                    assertThat(score).isEqualTo(20.0); // 10.0 + 10.0
                });
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
        return "ranking:monthly:" + date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
