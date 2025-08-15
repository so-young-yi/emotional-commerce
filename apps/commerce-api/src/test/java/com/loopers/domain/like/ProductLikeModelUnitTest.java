package com.loopers.domain.like;

import com.loopers.application.like.ProductLikeInfo;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;

class ProductLikeModelUnitTest {

    private ProductLikeRepository productLikeRepository;
    private ProductLikeService productLikeService;

    @BeforeEach
    void setUp() {
        productLikeRepository = Mockito.mock(ProductLikeRepository.class);
        productLikeService = new ProductLikeService(productLikeRepository);
    }

    @Nested
    @DisplayName("likeProduct (좋아요 등록)")
    class LikeProduct {

        @Test
        @DisplayName("아직 좋아요하지 않은 상품에 좋아요를 등록하면 저장되고 true를 반환한다")
        void likeProduct_whenNotLikedYet_shouldSaveAndReturnTrue() {

            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);
            boolean notExist = false;
            Mockito.when(productLikeRepository.existsByUserIdAndProductId(
                        productLikeInfo.userId(),
                        productLikeInfo.productId()
                    ))
                    .thenReturn(notExist);

            // act
            boolean result = productLikeService.likeProduct(productLikeInfo);

            // assert
            Mockito.verify(productLikeRepository).save(Mockito.any(ProductLikeModel.class));
            assertThat(result).isTrue();

        }

        @Test
        @DisplayName("이미 좋아요한 상품에 다시 좋아요를 등록해도 저장하지 않고 true를 반환한다 (멱등성)")
        void likeProduct_whenAlreadyLiked_shouldNotSaveAndReturnTrue() {

            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);
            boolean alreadyExist = true;
            Mockito.when(productLikeRepository.existsByUserIdAndProductId(
                            productLikeInfo.userId(),
                            productLikeInfo.productId()
                    ))
                    .thenReturn(alreadyExist);
            // act
            boolean result = productLikeService.likeProduct(productLikeInfo);

            // assert
            Mockito.verify(productLikeRepository, Mockito.never()).save(Mockito.any(ProductLikeModel.class));
            assertThat(result).isTrue();

        }
    }

    @Nested
    @DisplayName("unlikeProduct (좋아요 해제)")
    class UnlikeProduct {

        @Test
        @DisplayName("좋아요한 상품을 해제하면 삭제되고 false를 반환한다")
        void unlikeProduct_whenLiked_shouldDeleteAndReturnFalse() {

            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);
            boolean alreadyExist = true;
            Mockito.when(productLikeRepository.existsByUserIdAndProductId(
                            productLikeInfo.userId(),
                            productLikeInfo.productId()
                    ))
                    .thenReturn(alreadyExist);
            // act
            boolean result = productLikeService.unlikeProduct(productLikeInfo);
            // assert
            Mockito.verify(productLikeRepository).delete(Mockito.any(Long.class),Mockito.any(Long.class));
            assertThat(result).isFalse();

        }

        @Test
        @DisplayName("좋아요하지 않은 상품을 해제해도 아무 동작 없이 false를 반환한다 (멱등성)")
        void unlikeProduct_whenNotLiked_shouldNotDeleteAndReturnFalse() {
            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);

            // act
            boolean result = productLikeService.unlikeProduct(productLikeInfo);

            // assert
            Mockito.verify(productLikeRepository).delete(productLikeInfo.userId(), productLikeInfo.productId());
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isProductLiked (좋아요 여부 확인)")
    class IsProductLiked {

        @Test
        @DisplayName("좋아요한 상품이면 true를 반환한다")
        void isProductLiked_whenLiked_shouldReturnTrue() {

            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);
            boolean like = true;
            Mockito.when(productLikeRepository.existsByUserIdAndProductId(
                            productLikeInfo.userId(),
                            productLikeInfo.productId()
                    ))
                    .thenReturn(like);
            // act
            boolean result = productLikeService.isProductLiked(productLikeInfo);
            // assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("좋아요하지 않은 상품이면 false를 반환한다")
        void isProductLiked_whenNotLiked_shouldReturnFalse() {

            // arrange
            ProductLikeInfo productLikeInfo = new ProductLikeInfo(1L, 1L);
            boolean like = false;
            Mockito.when(productLikeRepository.existsByUserIdAndProductId(
                            productLikeInfo.userId(),
                            productLikeInfo.productId()
                    ))
                    .thenReturn(like);
            // act
            boolean result = productLikeService.isProductLiked(productLikeInfo);
            // assert
            assertThat(result).isFalse();
        }
    }
}
