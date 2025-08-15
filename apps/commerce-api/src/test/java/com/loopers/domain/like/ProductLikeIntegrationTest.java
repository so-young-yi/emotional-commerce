package com.loopers.domain.like;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductLikeIntegrationTest {

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Nested
    @DisplayName("상품 좋아요 등록/해제 통합 테스트")
    class LikeAndUnlike {

        @Test
        @DisplayName("상품에 좋아요를 등록하면 DB에 저장된다")
        void likeProduct_shouldSaveToDatabase() {
            // arrange
            Long userId = 1L;
            Long productId = 2L;
            ProductLikeModel like = new ProductLikeModel(userId, productId);

            // act
            productLikeRepository.save(like);

            // assert
            boolean exists = productLikeRepository.existsByUserIdAndProductId(userId, productId);
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("상품 좋아요 해제 시 DB에서 삭제된다")
        void unlikeProduct_shouldDeleteFromDatabase() {
            // arrange
            Long userId = 1L;
            Long productId = 2L;
            ProductLikeModel like = new ProductLikeModel(userId, productId);
            productLikeRepository.save(like);

            // act
            productLikeRepository.delete(userId, productId);

            // assert
            boolean exists = productLikeRepository.existsByUserIdAndProductId(userId, productId);
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("좋아요 수/목록 통합 테스트")
    class LikeCountAndList {

        @Test
        @DisplayName("상품의 좋아요 수를 정확히 조회할 수 있다")
        void countByProductId_shouldReturnCorrectCount() {
            // arrange
            Long productId = 10L;
            productLikeRepository.save(new ProductLikeModel(1L, productId));
            productLikeRepository.save(new ProductLikeModel(2L, productId));

            // act
            long count = productLikeRepository.countByProductId(productId);

            // assert
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("사용자가 좋아요한 상품 목록을 조회할 수 있다")
        void findProductIdByUserId_shouldReturnLikedProductIds() {
            // arrange
            Long userId = 1L;
            productLikeRepository.save(new ProductLikeModel(userId, 100L));
            productLikeRepository.save(new ProductLikeModel(userId, 200L));

            // act
            List<Long> productList = productLikeRepository.findProductIdByUserId(userId).stream().map(ProductLikeModel::getProductId).toList();

            // assert
            assertThat(productList).containsExactlyInAnyOrder(100L, 200L);
        }
    }
}
