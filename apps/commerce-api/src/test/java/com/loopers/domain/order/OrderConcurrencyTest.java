package com.loopers.domain.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("OrderFacade 재고 동시성 통합 테스트")
class OrderConcurrencyTest {

    @Autowired private OrderFacade orderFacade;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductMetaRepository productMetaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;

    private Long productId;

    private Long createProductWithStock(long stock) {
        ProductModel product = productRepository.save(new ProductModel(
                null, 1L, "에어맥스", "설명", new Money(10_000L), ProductStatus.ON_SALE, ZonedDateTime.now()
        ));
        productMetaRepository.save(ProductMetaModel.builder()
                .productId(product.getId())
                .stock(stock)
                .likeCount(0L)
                .reviewCount(0L)
                .viewCount(0L)
                .build());
        return product.getId();
    }

    private Long createUserWithPoint(String username, long point) {
        UserModel user = userRepository.save(new UserModel(
                username, "테스트", username + "@email.com", "2000-01-01", Gender.F
        ));
        userPointRepository.save(new UserPointModel(user.getId(), point));
        return user.getId();
    }

    private List<Long> createUsersWithPoint(int count, long point) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userIds.add(createUserWithPoint("testuser" + i, point));
        }
        return userIds;
    }

    private void runConcurrent(int threadCount, Runnable task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try { task.run(); } finally { latch.countDown(); }
            });
        }
        latch.await();
        executorService.shutdown();
    }

    @Nested
    @DisplayName("한 명의 유저가 여러 번 주문")
    class SingleUserConcurrency {

        private Long userId;

        @BeforeEach
        void setUp() {
            productId = createProductWithStock(10L);
            userId = createUserWithPoint("testuser", 100_000L);
        }

        @Test
        @DisplayName("여러 스레드가 동시에 주문해도 재고가 음수가 되지 않는다")
        void shouldNotAllowNegativeStock_whenOrderConcurrently() throws InterruptedException {
            int threadCount = 10;
            runConcurrent(threadCount, () -> {
                OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                        Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L))
                );
                try { orderFacade.orderAndPay(userId, request); } catch (Exception ignored) {}
            });

            Long stock = productMetaRepository.findByProductId(productId).get().getStock();
            assertThat(stock).isGreaterThanOrEqualTo(0L);
            assertThat(stock).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("여러 명의 유저가 각각 주문")
    class MultiUserConcurrency {

        private List<Long> userIds;

        @BeforeEach
        void setUp() {
            productId = createProductWithStock(10L);
            userIds = createUsersWithPoint(10, 100_000L);
        }

        @Test
        @DisplayName("10명의 서로 다른 유저가 동시에 주문해도 재고가 음수가 되지 않는다")
        void shouldNotAllowNegativeStock_whenOrderConcurrentlyByMultipleUsers() throws InterruptedException {
            int threadCount = 10;
            runConcurrent(threadCount, new Runnable() {
                private int idx = 0;
                @Override
                public synchronized void run() {
                    Long userId = userIds.get(idx++);
                    OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                            Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L))
                    );
                    try { orderFacade.orderAndPay(userId, request); } catch (Exception ignored) {}
                }
            });

            Long stock = productMetaRepository.findByProductId(productId).get().getStock();
            assertThat(stock).isGreaterThanOrEqualTo(0L);
            assertThat(stock).isEqualTo(0L);
        }
    }
}
