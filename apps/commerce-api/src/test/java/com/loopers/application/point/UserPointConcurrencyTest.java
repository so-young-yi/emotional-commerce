package com.loopers.application.point;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
import com.loopers.domain.point.UserPointService;
import com.loopers.domain.product.*;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.point.UserPointJpaRepository;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.Money;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("UserPoint 동시성 테스트")
class UserPointConcurrencyTest {

    @Autowired private OrderFacade orderFacade;
    @Autowired private UserPointService userPointService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;
    @Autowired private UserPointJpaRepository userPointJpaRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductStockRepository productStockRepository;

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @AfterEach void tearDown() { databaseCleanUp.truncateAllTables(); }

    private Long createUserWithPoint(long point) {
        UserModel user = userRepository.save(new UserModel("user", "유저", "user@email.com", "2000-01-01", Gender.F));
        userPointRepository.save(new UserPointModel(user.getId(), point));
        return user.getId();
    }

    private Long createProductWithStock(long stock) {
        ProductModel product = productRepository.save(new ProductModel(
                null, 1L, "상품", "설명", new Money(10_000L), ProductStatus.ON_SALE, ZonedDateTime.now()
        ));
        productStockRepository.save(ProductStockModel.builder()
                .productId(product.getId())
                .stock(stock)
                .build());
        return product.getId();
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
    @DisplayName("동시 충전")
    class ConcurrentCharge {
        private Long userId;

        @BeforeEach
        void setUp() {
            userId = createUserWithPoint(1000L);
        }

        @Test
        @DisplayName("여러 스레드가 동시에 충전해도 잔액이 정확히 누적된다")
        void shouldAccumulateBalanceCorrectly_whenChargedConcurrently() throws InterruptedException {
            int threadCount = 10;
            long chargeAmount = 100L;
            runConcurrent(threadCount, () -> userPointService.chargeUserPointWithLock(userId, chargeAmount));
            Long balance = userPointJpaRepository.findByUserId(userId).get().getBalance();
            assertThat(balance).isEqualTo(1000L + threadCount * chargeAmount);
        }
    }

    @Nested
    @DisplayName("동시 차감")
    class ConcurrentUse {
        private Long userId;

        @BeforeEach
        void setUp() {
            userId = createUserWithPoint(1000L);
        }

        @Test
        @DisplayName("여러 스레드가 동시에 차감해도 잔액이 음수가 되지 않는다")
        void shouldNotAllowNegativeBalance_whenUsedConcurrently() throws InterruptedException {
            int threadCount = 10;
            long useAmount = 100L;
            runConcurrent(threadCount, () -> {
                try { userPointService.useUserPointWithLock(userId, useAmount); } catch (Exception ignored) {}
            });
            Long balance = userPointJpaRepository.findByUserId(userId).get().getBalance();
            assertThat(balance).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("주문시 포인트 동시성 테스트")
    class OrderAndPay {
        private Long productId;
        private Long userId;

        @BeforeEach
        void setUp() {
            userId = createUserWithPoint(10_000L);
            productId = createProductWithStock(10L);
        }

        @Test
        @DisplayName("동일한 유저가 여러 주문을 동시에 수행해도 포인트가 음수가 되지 않는다")
        void shouldNotAllowNegativePoint_whenOrderConcurrently() throws InterruptedException {
            int threadCount = 10;
            OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                    Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L,null)),null
            );
            runConcurrent(threadCount, () -> {
                try {
                    orderFacade.orderAndPay(userId, request);
                } catch (Exception ignored) {}
            });
            Long point = userPointRepository.findByUserId(userId).get().getBalance();
            assertThat(point).isEqualTo(0L);
        }
    }
}
