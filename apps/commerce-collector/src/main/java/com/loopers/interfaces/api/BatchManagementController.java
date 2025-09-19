package com.loopers.interfaces.api;

import com.loopers.interfaces.batch.scheduler.RankingAggregationScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchManagementController {

    private final RankingAggregationScheduler scheduler;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @Qualifier("redisTemplateMaster")
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 배치 작업 수동 실행
     */
    @PostMapping("/trigger/{jobType}")
    public ResponseEntity<Map<String, Object>> triggerBatchJob(@PathVariable String jobType) {
        Map<String, Object> response = new HashMap<>();

        try {
            switch (jobType.toLowerCase()) {
                case "daily-to-weekly" -> {
                    scheduler.runDailyToWeeklyAggregationManually();
                    response.put("message", "Daily to weekly aggregation job triggered successfully");
                }
                case "weekly-to-monthly" -> {
                    scheduler.runWeeklyToMonthlyAggregationManually();
                    response.put("message", "Weekly to monthly aggregation job triggered successfully");
                }
                case "redis-to-weekly-mv" -> {
                    scheduler.runRedisToMaterializedViewManually();
                    response.put("message", "Redis to weekly MV job triggered successfully");
                }
                case "redis-to-monthly-mv" -> {
                    scheduler.runRedisToMonthlyMaterializedViewManually();
                    response.put("message", "Redis to monthly MV job triggered successfully");
                }
                default -> {
                    response.put("error", "Unknown job type: " + jobType);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger batch job: {}", jobType, e);
            response.put("error", "Failed to trigger job: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 배치 작업 실행 이력 조회
     */
    @GetMapping("/history/{jobName}")
    public ResponseEntity<List<JobExecution>> getBatchHistory(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<JobExecution> executions = jobExplorer.getJobInstances(jobName, 0, limit)
                    .stream()
                    .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                    .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
                    .limit(limit)
                    .toList();

            return ResponseEntity.ok(executions);

        } catch (Exception e) {
            log.error("Failed to get batch history for job: {}", jobName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Redis 랭킹 상태 모니터링
     */
    @GetMapping("/redis/status")
    public ResponseEntity<Map<String, Object>> getRedisRankingStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            LocalDate today = LocalDate.now();
            String dailyKey = "ranking:daily:" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String weeklyKey = getWeeklyRankingKey(today);
            String monthlyKey = getMonthlyRankingKey(today);

            status.put("daily", getRankingKeyInfo(dailyKey));
            status.put("weekly", getRankingKeyInfo(weeklyKey));
            status.put("monthly", getRankingKeyInfo(monthlyKey));

            status.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Failed to get Redis ranking status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 랭킹 데이터 초기화 (개발용)
     */
    @DeleteMapping("/redis/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupRedisRanking(
            @RequestParam(required = false) String keyPattern) {

        Map<String, Object> response = new HashMap<>();

        try {
            String pattern = keyPattern != null ? keyPattern : "ranking:*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                response.put("deletedKeys", keys.size());
                response.put("message", "Cleanup completed successfully");
            } else {
                response.put("message", "No keys found matching pattern: " + pattern);
                response.put("deletedKeys", 0);
            }

            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to cleanup Redis ranking data", e);
            response.put("error", "Failed to cleanup: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 시스템 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Redis 연결 확인
            String redisStatus = redisTemplate.getConnectionFactory().getConnection().ping();
            health.put("redis", "PONG".equals(redisStatus) ? "UP" : "DOWN");

            // 최근 배치 실행 상태 확인
            health.put("lastBatchExecution", getLastBatchExecutionStatus());

            health.put("timestamp", LocalDateTime.now());
            health.put("status", "UP");

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    private Map<String, Object> getRankingKeyInfo(String key) {
        Map<String, Object> info = new HashMap<>();
        info.put("key", key);
        info.put("exists", redisTemplate.hasKey(key));

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            info.put("size", redisTemplate.opsForZSet().size(key));
            info.put("ttl", redisTemplate.getExpire(key));

            // Top 5 상품 조회
            Set<String> top5 = redisTemplate.opsForZSet().reverseRange(key, 0, 4);
            info.put("top5", top5);
        }

        return info;
    }

    private String getWeeklyRankingKey(LocalDate date) {
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfYear());
        return String.format("ranking:weekly:%d-%02d", year, week);
    }

    private String getMonthlyRankingKey(LocalDate date) {
        return "ranking:monthly:" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private Map<String, Object> getLastBatchExecutionStatus() {
        try {
            // 최근 실행된 배치 작업들의 상태 조회
            Map<String, Object> lastExecution = new HashMap<>();

            String[] jobNames = {
                "dailyToWeeklyAggregationJob",
                "weeklyToMonthlyAggregationJob",
                "redisToMaterializedViewJob"
            };

            for (String jobName : jobNames) {
                List<JobExecution> executions = jobExplorer.getJobInstances(jobName, 0, 1)
                        .stream()
                        .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                        .toList();

                if (!executions.isEmpty()) {
                    JobExecution latest = executions.get(0);
                    Map<String, Object> executionInfo = new HashMap<>();
                    executionInfo.put("status", latest.getStatus().toString());
                    executionInfo.put("startTime", latest.getStartTime());
                    executionInfo.put("endTime", latest.getEndTime());
                    lastExecution.put(jobName, executionInfo);
                }
            }

            return lastExecution;

        } catch (Exception e) {
            log.error("Failed to get last batch execution status", e);
            return Map.of("error", "Failed to retrieve status");
        }
    }
}
