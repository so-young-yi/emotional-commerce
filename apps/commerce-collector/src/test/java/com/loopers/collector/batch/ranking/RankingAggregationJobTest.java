package com.loopers.collector.batch.ranking;

import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@Import(RedisTestContainersConfig.class)
class RankingAggregationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dailyToWeeklyAggregationJob")
    private Job dailyToWeeklyAggregationJob;

    @Autowired
    @Qualifier("weeklyToMonthlyAggregationJob") 
    private Job weeklyToMonthlyAggregationJob;

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
    @DisplayName("일간 데이터가 주간 데이터로 정상적으로 집계되어야 한다")
    void should_aggregate_daily_to_weekly_successfully() throws Exception {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = getDailyRankingKey(yesterday);
        String weeklyKey = getWeeklyRankingKey(yesterday);

        // 일간 랭킹 데이터 준비
        redisTemplate.opsForZSet().add(dailyKey, "product1", 10.0);
        redisTemplate.opsForZSet().add(dailyKey, "product2", 20.0);
        redisTemplate.opsForZSet().add(dailyKey, "product3", 15.0);

        // 기존 주간 데이터가 있는 경우 (누적 테스트)
        redisTemplate.opsForZSet().add(weeklyKey, "product1", 5.0);

        // When
        jobLauncherTestUtils.setJob(dailyToWeeklyAggregationJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // 주간 데이터 검증
        Double product1Score = redisTemplate.opsForZSet().score(weeklyKey, "product1");
        Double product2Score = redisTemplate.opsForZSet().score(weeklyKey, "product2");
        Double product3Score = redisTemplate.opsForZSet().score(weeklyKey, "product3");

        assertThat(product1Score).isEqualTo(15.0); // 5.0 + 10.0 (기존 + 새로운)
        assertThat(product2Score).isEqualTo(20.0);
        assertThat(product3Score).isEqualTo(15.0);

        // TTL 설정 확인
        Long ttl = redisTemplate.getExpire(weeklyKey);
        assertThat(ttl).isGreaterThan(0);
    }

    @Test
    @DisplayName("주간 데이터가 월간 데이터로 정상적으로 집계되어야 한다")
    void should_aggregate_weekly_to_monthly_successfully() throws Exception {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate firstWeek = now.withDayOfMonth(1);
        LocalDate secondWeek = firstWeek.plusWeeks(1);
        
        String firstWeekKey = getWeeklyRankingKey(firstWeek);
        String secondWeekKey = getWeeklyRankingKey(secondWeek);
        String monthlyKey = getMonthlyRankingKey(now);

        // 주간 랭킹 데이터 준비
        redisTemplate.opsForZSet().add(firstWeekKey, "product1", 100.0);
        redisTemplate.opsForZSet().add(firstWeekKey, "product2", 80.0);
        
        redisTemplate.opsForZSet().add(secondWeekKey, "product1", 50.0);
        redisTemplate.opsForZSet().add(secondWeekKey, "product3", 70.0);

        // When
        jobLauncherTestUtils.setJob(weeklyToMonthlyAggregationJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // 월간 데이터 검증
        Double product1Score = redisTemplate.opsForZSet().score(monthlyKey, "product1");
        Double product2Score = redisTemplate.opsForZSet().score(monthlyKey, "product2");
        Double product3Score = redisTemplate.opsForZSet().score(monthlyKey, "product3");

        assertThat(product1Score).isEqualTo(150.0); // 100.0 + 50.0
        assertThat(product2Score).isEqualTo(80.0);
        assertThat(product3Score).isEqualTo(70.0);

        // TTL 설정 확인
        Long ttl = redisTemplate.getExpire(monthlyKey);
        assertThat(ttl).isGreaterThan(0);
    }

    @Test
    @DisplayName("일간 데이터가 없는 경우에도 배치가 정상적으로 완료되어야 한다")
    void should_complete_successfully_when_no_daily_data_exists() throws Exception {
        // Given - 일간 데이터 없음

        // When
        jobLauncherTestUtils.setJob(dailyToWeeklyAggregationJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    @DisplayName("주간 데이터가 없는 경우에도 배치가 정상적으로 완료되어야 한다")
    void should_complete_successfully_when_no_weekly_data_exists() throws Exception {
        // Given - 주간 데이터 없음

        // When
        jobLauncherTestUtils.setJob(weeklyToMonthlyAggregationJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    @DisplayName("여러 상품의 점수가 정확하게 누적되어야 한다")
    void should_accumulate_scores_correctly_for_multiple_products() throws Exception {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = getDailyRankingKey(yesterday);
        String weeklyKey = getWeeklyRankingKey(yesterday);

        // 다양한 점수의 상품들 준비
        redisTemplate.opsForZSet().add(dailyKey, "product1", 5.5);
        redisTemplate.opsForZSet().add(dailyKey, "product2", 3.2);
        redisTemplate.opsForZSet().add(dailyKey, "product3", 7.8);
        redisTemplate.opsForZSet().add(dailyKey, "product4", 1.1);
        redisTemplate.opsForZSet().add(dailyKey, "product5", 9.9);

        // When
        jobLauncherTestUtils.setJob(dailyToWeeklyAggregationJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Set<String> topProducts = redisTemplate.opsForZSet().reverseRange(weeklyKey, 0, 4);
        assertThat(topProducts).containsExactly("product5", "product3", "product1", "product2", "product4");
        
        // 점수 정확성 검증
        assertThat(redisTemplate.opsForZSet().score(weeklyKey, "product1")).isEqualTo(5.5);
        assertThat(redisTemplate.opsForZSet().score(weeklyKey, "product5")).isEqualTo(9.9);
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
