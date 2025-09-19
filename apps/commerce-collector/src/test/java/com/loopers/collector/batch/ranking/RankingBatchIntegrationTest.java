package com.loopers.collector.batch.ranking;

import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class RankingBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("redisTemplateMaster")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private Job redisToWeeklyMaterializedViewJob;

    @Autowired
    private Job redisToMonthlyMaterializedViewJob;

    @Autowired
    private Job dailyToWeeklyAggregationJob;

    @Autowired
    private Job weeklyToMonthlyAggregationJob;

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @Autowired private RedisCleanUp redisCleanUp;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @Test
    @DirtiesContext
    @DisplayName("Redis 일간→주간 집계 Job 테스트")
    void dailyToWeeklyAggregationJobTest() throws Exception {
        // Given: Redis 일간 데이터 준비
        setupDailyRedisData();

        // When: 일간→주간 집계 Job 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", LocalDate.now().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(dailyToWeeklyAggregationJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: Job 실행 성공 검증
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Then: 주간 집계 데이터 검증
        String weeklyKey = getWeeklyRankingKey(LocalDate.now().minusDays(1));
        Long productCount = redisTemplate.opsForZSet().zCard(weeklyKey);
        assertThat(productCount).isEqualTo(5);

        Double topScore = redisTemplate.opsForZSet().score(weeklyKey, "1001");
        assertThat(topScore).isEqualTo(150.5);

        System.out.println("일간→주간 집계 완료 - 주간 키: " + weeklyKey + ", 상품 수: " + productCount);
    }

    @Test
    @DirtiesContext
    @DisplayName("Redis 주간→월간 집계 Job 테스트")
    void weeklyToMonthlyAggregationJobTest() throws Exception {
        // Given: Redis 주간 데이터 준비
        setupWeeklyRedisData();

        // When: 주간→월간 집계 Job 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", LocalDate.now().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(weeklyToMonthlyAggregationJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: Job 실행 성공 검증
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Then: 월간 집계 데이터 검증
        String monthlyKey = getMonthlyRankingKey(LocalDate.now());
        Long productCount = redisTemplate.opsForZSet().zCard(monthlyKey);
        assertThat(productCount).isGreaterThan(0);

        System.out.println("주간→월간 집계 완료 - 월간 키: " + monthlyKey + ", 상품 수: " + productCount);
    }

    @Test
    @DirtiesContext
    @DisplayName("Redis→DB 주간 MV 영속화 Job 테스트")
    void redisToWeeklyMaterializedViewJobTest() throws Exception {
        // Given: Redis 주간 데이터 준비
        setupWeeklyRedisData();

        // When: Redis→DB 주간 MV Job 실행
        String targetDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(redisToWeeklyMaterializedViewJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: Job 실행 성공 검증
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Then: 주간 MV 데이터 검증
        Integer weeklyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly", Integer.class);
        assertThat(weeklyCount).isEqualTo(5);

        BigDecimal topScore = jdbcTemplate.queryForObject(
                "SELECT ranking_score FROM mv_product_rank_weekly WHERE product_id = 1001",
                BigDecimal.class);
        assertThat(topScore).isEqualByComparingTo(BigDecimal.valueOf(150.5));

        System.out.println("Redis→주간 MV 영속화 완료 - 생성된 레코드 수: " + weeklyCount);
    }

    @Test
    @DirtiesContext
    @DisplayName("Redis→DB 월간 MV 영속화 Job 테스트")
    void redisToMonthlyMaterializedViewJobTest() throws Exception {
        // Given: Redis 월간 데이터 준비
        setupMonthlyRedisData();

        // When: Redis→DB 월간 MV Job 실행
        String targetDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(redisToMonthlyMaterializedViewJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: Job 실행 성공 검증
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Then: 월간 MV 데이터 검증
        Integer monthlyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_monthly", Integer.class);
        assertThat(monthlyCount).isEqualTo(5);

        System.out.println("Redis→월간 MV 영속화 완료 - 생성된 레코드 수: " + monthlyCount);
    }

    @Test
    @DirtiesContext
    @DisplayName("Step별 단독 실행 테스트 - 주간 MV Step")
    void redisToWeeklyMaterializedViewStepTest() throws Exception {
        // Given: Redis 주간 데이터 준비
        setupWeeklyRedisData();

        // Given: Job 설정
        jobLauncherTestUtils.setJob(redisToWeeklyMaterializedViewJob);

        // When: Step만 실행
        String targetDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("redisToWeeklyMaterializedViewStep", jobParameters);

        // Then: Step 실행 성공 검증
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Then: 데이터 검증
        Integer weeklyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly", Integer.class);
        assertThat(weeklyCount).isEqualTo(5);

        System.out.println("주간 MV Step 실행 완료 - 생성된 레코드 수: " + weeklyCount);
    }

    private void setupDailyRedisData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = getDailyRankingKey(yesterday);

        redisTemplate.opsForZSet().add(dailyKey, "1001", 150.5);
        redisTemplate.opsForZSet().add(dailyKey, "1002", 120.3);
        redisTemplate.opsForZSet().add(dailyKey, "1003", 95.7);
        redisTemplate.opsForZSet().add(dailyKey, "1004", 80.2);
        redisTemplate.opsForZSet().add(dailyKey, "1005", 65.1);

        System.out.println("Redis 일간 테스트 데이터 설정 완료 - Key: " + dailyKey);
    }

    private void setupWeeklyRedisData() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        String weeklyKey = getWeeklyRankingKey(targetDate);

        redisTemplate.opsForZSet().add(weeklyKey, "1001", 150.5);
        redisTemplate.opsForZSet().add(weeklyKey, "1002", 120.3);
        redisTemplate.opsForZSet().add(weeklyKey, "1003", 95.7);
        redisTemplate.opsForZSet().add(weeklyKey, "1004", 80.2);
        redisTemplate.opsForZSet().add(weeklyKey, "1005", 65.1);

        System.out.println("Redis 주간 테스트 데이터 설정 완료 - Key: " + weeklyKey);
    }

    private void setupMonthlyRedisData() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String monthlyKey = getMonthlyRankingKey(lastMonth);

        redisTemplate.opsForZSet().add(monthlyKey, "1001", 450.5);
        redisTemplate.opsForZSet().add(monthlyKey, "1002", 360.9);
        redisTemplate.opsForZSet().add(monthlyKey, "1003", 287.1);
        redisTemplate.opsForZSet().add(monthlyKey, "1004", 240.6);
        redisTemplate.opsForZSet().add(monthlyKey, "1005", 195.3);

        System.out.println("Redis 월간 테스트 데이터 설정 완료 - Key: " + monthlyKey);
    }

    private String getDailyRankingKey(LocalDate date) {
        return "ranking:daily:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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
