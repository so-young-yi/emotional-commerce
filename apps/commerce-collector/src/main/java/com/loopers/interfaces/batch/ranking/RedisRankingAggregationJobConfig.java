package com.loopers.interfaces.batch.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 랭킹 집계 배치 작업 설정 클래스
 *
 * <p>Redis 내에서의 일간 → 주간 → 월간 랭킹 집계를 담당합니다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisRankingAggregationJobConfig {

    @Qualifier("redisTemplateMaster")
    private final RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 일간 → 주간 Redis 집계 작업
     */
    @Bean
    public Job dailyToWeeklyAggregationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("dailyToWeeklyAggregationJob", jobRepository)
                .start(aggregateDailyToWeeklyStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * 주간 → 월간 Redis 집계 작업
     */
    @Bean
    public Job weeklyToMonthlyAggregationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("weeklyToMonthlyAggregationJob", jobRepository)
                .start(aggregateWeeklyToMonthlyStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * 일간 → 주간 Redis 집계 Step
     */
    @Bean
    public Step aggregateDailyToWeeklyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aggregateDailyToWeeklyStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Starting daily to weekly ranking aggregation");

                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    String dailyKey = getDailyRankingKey(yesterday);
                    String weeklyKey = getWeeklyRankingKey(yesterday);

                    // 일간 랭킹 데이터를 주간 랭킹에 병합
                    Set<String> productIds = redisTemplate.opsForZSet().range(dailyKey, 0, -1);

                    if (productIds != null && !productIds.isEmpty()) {
                        for (String productId : productIds) {
                            Double dailyScore = redisTemplate.opsForZSet().score(dailyKey, productId);
                            if (dailyScore != null) {
                                redisTemplate.opsForZSet().incrementScore(weeklyKey, productId, dailyScore);
                            }
                        }

                        // 주간 랭킹 TTL 설정
                        redisTemplate.expire(weeklyKey, 35, TimeUnit.DAYS);

                        log.info("Aggregated {} products from daily key {} to weekly key {}",
                                productIds.size(), dailyKey, weeklyKey);
                    } else {
                        log.info("No daily ranking data found for key: {}", dailyKey);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 주간 → 월간 Redis 집계 Step
     */
    @Bean
    public Step aggregateWeeklyToMonthlyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aggregateWeeklyToMonthlyStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Starting weekly to monthly ranking aggregation");

                    LocalDate now = LocalDate.now();

                    // 현재 월의 모든 주차에 대해 집계
                    LocalDate firstDayOfMonth = now.withDayOfMonth(1);
                    LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

                    String monthlyKey = getMonthlyRankingKey(now);

                    // 해당 월의 모든 주간 데이터를 병합
                    LocalDate current = firstDayOfMonth;
                    int aggregatedWeeks = 0;

                    while (!current.isAfter(lastDayOfMonth)) {
                        String weeklyKey = getWeeklyRankingKey(current);
                        Set<String> productIds = redisTemplate.opsForZSet().range(weeklyKey, 0, -1);

                        if (productIds != null && !productIds.isEmpty()) {
                            for (String productId : productIds) {
                                Double weeklyScore = redisTemplate.opsForZSet().score(weeklyKey, productId);
                                if (weeklyScore != null) {
                                    redisTemplate.opsForZSet().incrementScore(monthlyKey, productId, weeklyScore);
                                }
                            }
                            aggregatedWeeks++;
                        }

                        current = current.plusWeeks(1);
                    }

                    if (aggregatedWeeks > 0) {
                        // 월간 랭킹 TTL 설정
                        redisTemplate.expire(monthlyKey, 400, TimeUnit.DAYS);

                        log.info("Aggregated {} weeks of data to monthly key {}", aggregatedWeeks, monthlyKey);
                    } else {
                        log.info("No weekly ranking data found for month: {}", monthlyKey);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 오래된 Redis 랭킹 데이터 정리 Step
     */
    @Bean
    public Step cleanupOldRankingDataStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("cleanupOldRankingDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Starting cleanup of old ranking data");

                    // 30일 이전의 일간 데이터 삭제
                    LocalDate cutoffDate = LocalDate.now().minusDays(30);
                    for (int i = 0; i < 7; i++) { // 일주일치 정리
                        String oldDailyKey = getDailyRankingKey(cutoffDate.minusDays(i));
                        if (redisTemplate.hasKey(oldDailyKey)) {
                            redisTemplate.delete(oldDailyKey);
                            log.info("Deleted old daily ranking key: {}", oldDailyKey);
                        }
                    }

                    // 90일 이전의 주간 데이터 삭제
                    LocalDate weekCutoffDate = LocalDate.now().minusDays(90);
                    for (int i = 0; i < 4; i++) { // 4주치 정리
                        String oldWeeklyKey = getWeeklyRankingKey(weekCutoffDate.minusWeeks(i));
                        if (redisTemplate.hasKey(oldWeeklyKey)) {
                            redisTemplate.delete(oldWeeklyKey);
                            log.info("Deleted old weekly ranking key: {}", oldWeeklyKey);
                        }
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
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
