package com.loopers.interfaces.consumer;

import com.loopers.collector.domain.event.EventHandled;
import com.loopers.collector.domain.event.EventHandledRepository;
import com.loopers.event.*;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.KafkaCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CollectorKafkaListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EventHandledRepository eventHandledRepository;

    @Autowired
    @Qualifier("redisTemplateMaster")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired private KafkaCleanUp kafkaCleanUp;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    @Autowired private RedisCleanUp redisCleanUp;

    @BeforeEach
    void setUp() {
//        kafkaCleanUp.truncateAllTopics();
//        redisCleanUp.truncateAll();
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("ProductViewedEvent 처리 시 Redis 일간 랭킹에 점수가 증가한다")
    void should_update_redis_ranking_when_product_viewed() {
        // Given
        String productId = "product-123";
        String userId = "user-456";
        ProductViewedEvent event = new ProductViewedEvent(userId, productId);

        // When
        kafkaTemplate.send("product-events", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 이벤트 처리 기록 확인
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();

            // Redis 일간 랭킹 점수 확인
            String dailyKey = getDailyRankingKey(LocalDate.now());
            Double score = redisTemplate.opsForZSet().score(dailyKey, productId);
            assertThat(score).isNotNull().isPositive();
        });
    }

    @Test
    @DisplayName("ProductLikedEvent 처리 시 Redis 일간 랭킹에 점수가 증가한다")
    void should_update_redis_ranking_when_product_liked() {
        // Given
        String productId = "product-123";
        String userId = "user-456";
        ProductLikedEvent event = new ProductLikedEvent(userId, productId);

        // When
        kafkaTemplate.send("product-events", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 이벤트 처리 기록 확인
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();

            // Redis 일간 랭킹 점수 확인
            String dailyKey = getDailyRankingKey(LocalDate.now());
            Double score = redisTemplate.opsForZSet().score(dailyKey, productId);
            assertThat(score).isNotNull().isPositive();
        });
    }

    @Test
    @DisplayName("OrderCreatedEvent 처리 시 모든 주문 상품의 Redis 랭킹이 업데이트된다")
    void should_update_redis_ranking_for_all_ordered_products() {
        // Given
        String userId = "user-123";
        String orderId = "order-456";
        List<OrderCreatedEvent.OrderItem> items = List.of(
            new OrderCreatedEvent.OrderItem("product-1", 2, 10000L),
            new OrderCreatedEvent.OrderItem("product-2", 1, 20000L)
        );
        OrderCreatedEvent event = new OrderCreatedEvent(userId, orderId, items, 30000L);

        // When
        kafkaTemplate.send("order-events", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 이벤트 처리 기록 확인
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();

            // 각 상품의 Redis 일간 랭킹 점수 확인
            String dailyKey = getDailyRankingKey(LocalDate.now());
            System.out.println("Checking daily key: " + dailyKey);

            // Redis에 어떤 키들이 있는지 확인
            var allKeys = redisTemplate.keys("ranking:*");
            System.out.println("All ranking keys: " + allKeys);

            // ZSET 멤버들 확인
            var members = redisTemplate.opsForZSet().range(dailyKey, 0, -1);
            System.out.println("ZSET members for " + dailyKey + ": " + members);

            Double score1 = redisTemplate.opsForZSet().score(dailyKey, "product-1");
            System.out.println("Score for product-1: " + score1);
            assertThat(score1).isNotNull().isPositive();

            Double score2 = redisTemplate.opsForZSet().score(dailyKey, "product-2");
            System.out.println("Score for product-2: " + score2);
            assertThat(score2).isNotNull().isPositive();
        });
    }

    @Test
    @DisplayName("PaymentCompletedEvent 처리 시 이벤트가 기록된다")
    void should_record_payment_completed_event() {
        // Given
        String userId = "user-123";
        String orderId = "order-456";
        String paymentId = "payment-789";
        PaymentCompletedEvent event = new PaymentCompletedEvent(userId, orderId, paymentId, 30000L);

        // When
        kafkaTemplate.send("payment-events", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();
        });
    }

    @Test
    @DisplayName("PaymentFailedEvent 처리 시 이벤트가 기록된다")
    void should_record_payment_failed_event() {
        // Given
        String userId = "user-123";
        String orderId = "order-456";
        String paymentId = "payment-789";
        PaymentFailedEvent event = new PaymentFailedEvent(userId, orderId, "INSUFFICIENT_FUNDS");

        // When
        kafkaTemplate.send("payment-events", event);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();
        });
    }

    @Test
    @DisplayName("중복 이벤트는 무시되고 Redis 랭킹이 중복 업데이트되지 않는다")
    void should_ignore_duplicate_events_and_not_update_ranking_twice() {
        // Given
        String productId = "product-123";
        String userId = "user-456";
        ProductViewedEvent event = new ProductViewedEvent(userId, productId);

        // 첫 번째 이벤트 전송
        kafkaTemplate.send("product-events", event);

        // 첫 번째 이벤트 처리 대기
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(eventHandledRepository.existsByEventId(event.getEventId())).isTrue();
        });

        // 첫 번째 처리 후 Redis 점수 확인
        String dailyKey = getDailyRankingKey(LocalDate.now());
        Double firstScore = redisTemplate.opsForZSet().score(dailyKey, productId);
        assertThat(firstScore).isNotNull().isPositive();

        // When: 동일한 이벤트 재전송
        kafkaTemplate.send("product-events", event);

        // Then: 점수가 중복 증가하지 않음
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Double secondScore = redisTemplate.opsForZSet().score(dailyKey, productId);
            assertThat(secondScore).isEqualTo(firstScore);
        });
    }

    @Test
    @DisplayName("이벤트 처리 기록이 데이터베이스에 정확히 저장된다")
    void should_save_event_handled_records_correctly() {
        // Given
        ProductViewedEvent event1 = new ProductViewedEvent("user-1", "product-1");
        ProductLikedEvent event2 = new ProductLikedEvent("user-2", "product-2");

        // When
        kafkaTemplate.send("product-events", event1);
        kafkaTemplate.send("product-events", event2);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<EventHandled> handledEvents = eventHandledRepository.findAll();
            assertThat(handledEvents).hasSize(2);

            List<String> eventIds = handledEvents.stream()
                .map(EventHandled::getEventId)
                .toList();
            assertThat(eventIds).contains(event1.getEventId(), event2.getEventId());
        });
    }

    private String getDailyRankingKey(LocalDate date) {
        return "ranking:daily:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

}
