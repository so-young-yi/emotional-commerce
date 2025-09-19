package com.loopers.interfaces.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingAggregationScheduler {

    private final JobLauncher jobLauncher;

    // Redis 집계 Jobs (RedisRankingAggregationJobConfig)
    private final Job dailyToWeeklyAggregationJob;
    private final Job weeklyToMonthlyAggregationJob;

    // Redis → DB 영속화 Jobs (RedisToDatabaseJobConfig)
    private final Job redisToWeeklyMaterializedViewJob;
    private final Job redisToMonthlyMaterializedViewJob;

    /**
     * 매일 새벽 1시에 일간 -> 주간 집계 실행
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void aggregateDailyToWeekly() {
        try {
            log.info("Starting daily to weekly ranking aggregation job");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();
            
            jobLauncher.run(dailyToWeeklyAggregationJob, jobParameters);
            
            log.info("Daily to weekly ranking aggregation job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute daily to weekly ranking aggregation job", e);
        }
    }

    /**
     * 매주 일요일 새벽 2시에 주간 -> 월간 집계 실행
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void aggregateWeeklyToMonthly() {
        try {
            log.info("Starting weekly to monthly ranking aggregation job");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();
            
            jobLauncher.run(weeklyToMonthlyAggregationJob, jobParameters);
            
            log.info("Weekly to monthly ranking aggregation job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute weekly to monthly ranking aggregation job", e);
        }
    }

    /**
     * 매일 새벽 3시에 Redis -> DB materialized view 집계 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void aggregateRedisToMaterializedView() {
        try {
            log.info("Starting Redis to materialized view aggregation job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", LocalDate.now().minusDays(1).toString())
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(redisToWeeklyMaterializedViewJob, jobParameters);

            log.info("Redis to materialized view aggregation job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute Redis to materialized view aggregation job", e);
        }
    }

    /**
     * 수동 실행을 위한 메서드 (테스트용)
     */
    public void runDailyToWeeklyAggregationManually() {
        aggregateDailyToWeekly();
    }

    /**
     * 수동 실행을 위한 메서드 (테스트용)
     */
    public void runWeeklyToMonthlyAggregationManually() {
        aggregateWeeklyToMonthly();
    }

    /**
     * 매월 1일 새벽 4시에 Redis 월간 랭킹 -> DB materialized view 영속화 실행
     */
    @Scheduled(cron = "0 0 4 1 * *")
    public void aggregateRedisToMonthlyMaterializedView() {
        try {
            log.info("Starting Redis monthly ranking to materialized view aggregation job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", LocalDate.now().minusMonths(1).toString())
                    .addString("executionTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(redisToMonthlyMaterializedViewJob, jobParameters);

            log.info("Redis monthly ranking to materialized view aggregation job completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute Redis monthly ranking to materialized view aggregation job", e);
        }
    }

    /**
     * 수동 실행을 위한 메서드 (테스트용)
     */
    public void runRedisToMaterializedViewManually() {
        aggregateRedisToMaterializedView();
    }

    /**
     * 수동 실행을 위한 메서드 (테스트용)
     */
    public void runRedisToMonthlyMaterializedViewManually() {
        aggregateRedisToMonthlyMaterializedView();
    }
}
