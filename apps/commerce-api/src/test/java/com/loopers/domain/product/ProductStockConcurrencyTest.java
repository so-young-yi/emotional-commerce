package com.loopers.domain.product;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.point.UserPointModel;
import com.loopers.domain.point.UserPointRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("Product 재고 동시성 테스트")
class ProductStockConcurrencyTest {

    @Autowired private ProductMetaService productMetaService;
    @Autowired private ProductMetaRepository productMetaRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;
    @Autowired private OrderFacade orderFacade;

    private Long createProductWithStock(long stock) {
        ProductModel product = productRepository.save(new ProductModel(
                null, 1L, "상품", "설명", new Money(10_000L), ProductStatus.ON_SALE, ZonedDateTime.now()
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

    private List<Long> createUsersWithPoints(int count, long point) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UserModel user = userRepository.save(new UserModel(
                    "user" + i, "유저" + i, "user" + i + "@email.com", "2000-01-01", Gender.F
            ));
            userIds.add(user.getId());
            userPointRepository.save(new UserPointModel(user.getId(), point));
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
    @DisplayName("직접 재고 차감 동시성")
    class DirectDecreaseStock {

        private Long productId;

        @BeforeEach
        void setUp() {
            productId = createProductWithStock(10L);
        }

        @Test
        @DisplayName("여러 스레드가 동시에 재고를 차감해도 음수가 되지 않는다")
        void shouldNotAllowNegativeStock_whenDecreasedConcurrently() throws InterruptedException {
            int threadCount = 10;
            long decreaseAmount = 1L;
            runConcurrent(threadCount, () -> {
                try {
                    productMetaService.decreaseStock(productId, decreaseAmount);
                } catch (Exception ignored) {}
            });

            Long stock = productMetaService.getMeta(productId).getStock();
            assertThat(stock).isGreaterThanOrEqualTo(0L);
            assertThat(stock).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("주문을 통한 재고 차감 동시성")
    class OrderDecreaseStock {

        @Test
        @DisplayName("한 명의 유저가 여러 번 동시에 주문해도 재고가 음수가 되지 않는다")
        void shouldNotAllowNegativeStock_whenOneUserOrdersConcurrently() throws InterruptedException {
            Long productId = createProductWithStock(10L);
            Long userId = createUsersWithPoints(1, 100_000L).get(0);

            int threadCount = 10;
            runConcurrent(threadCount, () -> {
                OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                        Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L,null)),null
                );
                try {
                    orderFacade.orderAndPay(userId, request);
                } catch (Exception ignored) {}
            });

            Long stock = productMetaRepository.findByProductId(productId).get().getStock();
            assertThat(stock).isGreaterThanOrEqualTo(0L);
            assertThat(stock).isEqualTo(0L);
        }

        @Test
        @DisplayName("여러 명의 유저가 동시에 주문해도 재고가 음수가 되지 않는다")
        void shouldNotAllowNegativeStock_whenMultipleUsersOrderConcurrently() throws InterruptedException {
            Long productId = createProductWithStock(10L);
            List<Long> userIds = createUsersWithPoints(10, 100_000L);

            int threadCount = 10;
            runConcurrent(threadCount, () -> {
                int idx = (int) (Thread.currentThread().getId() % userIds.size());
                Long userId = userIds.get(idx);
                OrderV1Dto.OrderRequest request = new OrderV1Dto.OrderRequest(
                        Collections.singletonList(new OrderV1Dto.OrderItem(productId, 1L,null)),null
                );
                try {
                    orderFacade.orderAndPay(userId, request);
                } catch (Exception ignored) {}
            });

            Long stock = productMetaRepository.findByProductId(productId).get().getStock();
            assertThat(stock).isGreaterThanOrEqualTo(0L);
            assertThat(stock).isEqualTo(0L);
        }
    }
}
