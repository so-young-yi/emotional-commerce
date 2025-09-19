package com.loopers.interfaces.batch.ranking;

import com.loopers.collector.domain.ranking.RedisRankingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Redis → DB 영속화 배치 작업 설정 클래스
 *
 * <p>Redis에 저장된 랭킹 데이터를 DB Materialized Views로 영속화합니다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisToDatabaseJobConfig {

    @Qualifier("redisTemplateMaster")
    private final RedisTemplate<String, String> redisTemplate;

    private final DataSource dataSource;

    private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Redis → 주간 DB MV 영속화 작업
     */
    @Bean
    public Job redisToWeeklyMaterializedViewJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("redisToWeeklyMaterializedViewJob", jobRepository)
                .start(redisToWeeklyMaterializedViewStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * Redis → 월간 DB MV 영속화 작업
     */
    @Bean
    public Job redisToMonthlyMaterializedViewJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("redisToMonthlyMaterializedViewJob", jobRepository)
                .start(redisToMonthlyMaterializedViewStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * Redis → 주간 DB MV 영속화 Step
     */
    @Bean
    public Step redisToWeeklyMaterializedViewStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("redisToWeeklyMaterializedViewStep", jobRepository)
                .<RedisRankingItem, RedisRankingItem>chunk(100, transactionManager)
                .reader(redisWeeklyRankingReader(null))
                .processor(redisRankingScoreProcessor())
                .writer(redisWeeklyRankingWriter(null))
                .build();
    }

    /**
     * Redis → 월간 DB MV 영속화 Step
     */
    @Bean
    public Step redisToMonthlyMaterializedViewStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("redisToMonthlyMaterializedViewStep", jobRepository)
                .<RedisRankingItem, RedisRankingItem>chunk(100, transactionManager)
                .reader(redisMonthlyRankingReader(null))
                .processor(redisRankingScoreProcessor())
                .writer(redisMonthlyRankingWriter(null))
                .build();
    }


    /**
     * Redis 주간 랭킹 데이터 Reader
     */
    @Bean
    @StepScope
    public ItemReader<RedisRankingItem> redisWeeklyRankingReader(
            @Value("#{jobParameters[targetDate]}") String targetDate) {

        List<RedisRankingItem> items = new ArrayList<>();

        try {
            String weeklyKey = getWeeklyRankingKey(LocalDate.parse(targetDate));

            Set<ZSetOperations.TypedTuple<String>> rankingData =
                redisTemplate.opsForZSet().rangeWithScores(weeklyKey, 0, -1);

            if (rankingData != null) {
                for (ZSetOperations.TypedTuple<String> tuple : rankingData) {
                    String productId = tuple.getValue();
                    Double score = tuple.getScore();

                    if (productId != null && score != null) {
                        RedisRankingItem item = RedisRankingItem.builder()
                            .productId(Long.parseLong(productId))
                            .productName("Product-" + productId)
                            .date(LocalDate.parse(targetDate))
                            .weeklyScore(BigDecimal.valueOf(score))
                            .build();
                        items.add(item);
                    }
                }
            }

            log.info("Redis에서 {} 개의 주간 랭킹 데이터를 읽었습니다. Key: {}", items.size(), weeklyKey);

        } catch (Exception e) {
            log.error("Redis 주간 랭킹 데이터 읽기 실패: {}", e.getMessage(), e);
        }

        return new ListItemReader<>(items);
    }

    /**
     * Redis 월간 랭킹 데이터 Reader
     */
    @Bean
    @StepScope
    public ItemReader<RedisRankingItem> redisMonthlyRankingReader(
            @Value("#{jobParameters[targetDate]}") String targetDate) {

        List<RedisRankingItem> items = new ArrayList<>();

        try {
            String monthlyKey = getMonthlyRankingKey(LocalDate.parse(targetDate));

            Set<ZSetOperations.TypedTuple<String>> rankingData =
                redisTemplate.opsForZSet().rangeWithScores(monthlyKey, 0, -1);

            if (rankingData != null) {
                for (ZSetOperations.TypedTuple<String> tuple : rankingData) {
                    String productId = tuple.getValue();
                    Double score = tuple.getScore();

                    if (productId != null && score != null) {
                        RedisRankingItem item = RedisRankingItem.builder()
                            .productId(Long.parseLong(productId))
                            .productName("Product-" + productId)
                            .date(LocalDate.parse(targetDate))
                            .monthlyScore(BigDecimal.valueOf(score))
                            .build();
                        items.add(item);
                    }
                }
            }

            log.info("Redis에서 {} 개의 월간 랭킹 데이터를 읽었습니다. Key: {}", items.size(), monthlyKey);

        } catch (Exception e) {
            log.error("Redis 월간 랭킹 데이터 읽기 실패: {}", e.getMessage(), e);
        }

        return new ListItemReader<>(items);
    }


    /**
     * Redis 랭킹 데이터 Processor (단순 통과)
     */
    @Bean
    public ItemProcessor<RedisRankingItem, RedisRankingItem> redisRankingScoreProcessor() {
        return item -> {
            log.debug("Processing Redis ranking item - Product {}: Score = {}",
                item.getProductId(),
                item.getWeeklyScore() != null ? item.getWeeklyScore() : item.getMonthlyScore());
            return item;
        };
    }


    /**
     * 주간 랭킹 DB Writer
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<RedisRankingItem> redisWeeklyRankingWriter(
            @Value("#{jobParameters[targetDate]}") String targetDate) {

        String sql = """
            INSERT INTO mv_product_rank_weekly
            (product_id, product_name, week_start_date, ranking_score, view_count, like_count, order_count, total_order_amount, created_at)
            VALUES (?, ?, ?, ?, 0, 0, 0, 0, NOW())
            ON DUPLICATE KEY UPDATE
            ranking_score = VALUES(ranking_score),
            updated_at = NOW()
            """;

        return new JdbcBatchItemWriterBuilder<RedisRankingItem>()
                .dataSource(dataSource)
                .sql(sql)
                .itemPreparedStatementSetter((item, ps) -> {
                    LocalDate date = LocalDate.parse(targetDate);
                    LocalDate weekStart = getWeekStartDate(date);

                    ps.setLong(1, item.getProductId());
                    ps.setString(2, item.getProductName());
                    ps.setDate(3, java.sql.Date.valueOf(weekStart));
                    ps.setBigDecimal(4, item.getWeeklyScore());
                })
                .build();
    }

    /**
     * 월간 랭킹 DB Writer
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<RedisRankingItem> redisMonthlyRankingWriter(
            @Value("#{jobParameters[targetDate]}") String targetDate) {

        String sql = """
            INSERT INTO mv_product_rank_monthly
            (product_id, product_name, month_start_date, ranking_score, view_count, like_count, order_count, total_order_amount, created_at)
            VALUES (?, ?, ?, ?, 0, 0, 0, 0, NOW())
            ON DUPLICATE KEY UPDATE
            ranking_score = VALUES(ranking_score),
            updated_at = NOW()
            """;

        return new JdbcBatchItemWriterBuilder<RedisRankingItem>()
                .dataSource(dataSource)
                .sql(sql)
                .itemPreparedStatementSetter((item, ps) -> {
                    LocalDate date = LocalDate.parse(targetDate);
                    LocalDate monthStart = date.withDayOfMonth(1);

                    ps.setLong(1, item.getProductId());
                    ps.setString(2, item.getProductName());
                    ps.setDate(3, java.sql.Date.valueOf(monthStart));
                    ps.setBigDecimal(4, item.getMonthlyScore());
                })
                .build();
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

    private LocalDate getWeekStartDate(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.with(weekFields.dayOfWeek(), 1);
    }
}
