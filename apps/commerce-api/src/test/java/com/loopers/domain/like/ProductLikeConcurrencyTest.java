package com.loopers.domain.like;

import com.loopers.application.like.ProductLikeFacade;
import com.loopers.application.like.ProductLikeInfo;
import com.loopers.domain.product.ProductMetaModel;
import com.loopers.domain.product.ProductMetaRepository;
import com.loopers.domain.product.ProductMetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("ProductLike 동시성 테스트")
class ProductLikeConcurrencyTest {

    @Autowired private ProductLikeService productLikeService;
    @Autowired private ProductLikeRepository productLikeRepository;
    @Autowired private ProductMetaService productMetaService;
    @Autowired private ProductMetaRepository productMetaRepository;
    @Autowired private ProductLikeFacade productLikeFacade;

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

    private void saveProductMeta(Long productId) {
        productMetaRepository.save(ProductMetaModel.builder()
                .productId(productId)
                .stock(10L)
                .likeCount(0L)
                .reviewCount(0L)
                .viewCount(0L)
                .build());
    }

    @Nested
    @DisplayName("동시 좋아요 등록")
    class ConcurrentLike {
        private final Long userId = 1L;
        private final Long productId = 1L;

        @Test
        @DisplayName("여러 스레드가 동시에 좋아요를 눌러도 row는 1개만 생성된다")
        void shouldBeIdempotent_whenLikedConcurrently() throws InterruptedException {
            int threadCount = 10;
            runConcurrent(threadCount, () -> productLikeService.likeProduct(new ProductLikeInfo(userId, productId)));
            long count = productLikeRepository.countByProductId(productId);
            assertThat(count).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("동시 좋아요 해제")
    class ConcurrentUnlike {
        private final Long userId = 2L;
        private final Long productId = 2L;

        @BeforeEach
        void setUpLike() {
            productLikeService.likeProduct(new ProductLikeInfo(userId, productId));
        }

        @Test
        @DisplayName("여러 스레드가 동시에 해제해도 row는 0개만 남는다")
        void shouldBeIdempotent_whenUnlikedConcurrently() throws InterruptedException {
            int threadCount = 10;
            runConcurrent(threadCount, () -> productLikeService.unlikeProduct(new ProductLikeInfo(userId, productId)));
            long count = productLikeRepository.countByProductId(productId);
            assertThat(count).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("동시 좋아요 카운트")
    class ConcurrentLikeCount {
        private final Long productId = 3L;

        @Test
        @DisplayName("여러 사용자가 동시에 좋아요를 눌러도 카운트가 정확해야 한다")
        void shouldCountLikesCorrectly_whenLikedConcurrently() throws InterruptedException {
            int userCount = 10;
            Set<Long> userIds = new HashSet<>();
            for (long i = 1; i <= userCount; i++) userIds.add(i);

            runConcurrent(userCount, () -> {
                Long userId = userIds.stream().skip(Thread.currentThread().getId() % userCount).findFirst().orElse(1L);
                productLikeService.likeProduct(new ProductLikeInfo(userId, productId));
            });

            long count = productLikeRepository.countByProductId(productId);
            assertThat(count).isEqualTo(userCount);
        }
    }

    @Nested
    @DisplayName("좋아요 여러사용자 동시성 테스트")
    class ProductLikeMultiUser {
        private final Long productId = 1L;

        @BeforeEach
        void setUp() { saveProductMeta(productId); }

        @Test
        @DisplayName("동시에 여러명이 좋아요/싫어요를 반복해도 likeCount가 음수로 내려가지 않는다")
        void shouldNotAllowNegativeLikeCount_whenConcurrentLikeUnlike() throws InterruptedException {
            int threadCount = 20;
            runConcurrent(threadCount, () -> {
                boolean like = Thread.currentThread().getId() % 2 == 0;
                if (like) productMetaService.increaseLike(productId);
                else productMetaService.decreaseLike(productId);
            });
            Long likeCount = productMetaRepository.findByProductId(productId).get().getLikeCount();
            assertThat(likeCount).isGreaterThanOrEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("좋아요 파사드 동시성")
    class ProductLikeFacadeConcurrency {
        private final Long userId = 1L;
        private final Long productId = 1L;

        @BeforeEach
        void setUp() { saveProductMeta(productId); }

        @Test
        @DisplayName("동시에 여러명이 좋아요/싫어요를 반복해도 likeCount가 음수로 내려가지 않는다")
        void shouldNotAllowNegativeLikeCount_whenConcurrentLikeUnlike() throws InterruptedException {
            int threadCount = 20;
            runConcurrent(threadCount, () -> {
                boolean like = Thread.currentThread().getId() % 2 == 0;
                if (like) productLikeFacade.likeProduct(userId, productId);
                else productLikeFacade.unlikeProduct(userId, productId);
            });
            Long likeCount = productMetaRepository.findByProductId(productId).get().getLikeCount();
            assertThat(likeCount).isGreaterThanOrEqualTo(0L);
        }
    }
}
