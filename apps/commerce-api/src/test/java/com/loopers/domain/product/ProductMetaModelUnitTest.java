package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductMetaModelUnitTest {

    @DisplayName("재고가 음수면 예외 발생")
    @ParameterizedTest
    @ValueSource(longs = {-1, -100})
    void shouldFail_whenStockIsNegative(Long stock) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            ProductMetaModel.builder()
                    .productId(1L)
                    .stock(stock)
                    .likeCount(0L)
                    .reviewCount(0L)
                    .viewCount(0L)
                    .build();
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고 증가/감소 정상 동작")
    @Test
    void stock_increase_and_decrease() {
        ProductMetaModel meta = new ProductMetaModel(1L, 10L, 0L, 0L, 0L);

        meta.increaseStock(5L);
        assertThat(meta.getStock()).isEqualTo(15L);

        meta.decreaseStock(3L);
        assertThat(meta.getStock()).isEqualTo(12L);
    }

    @DisplayName("재고 증가: 0 이하로 증가 시 예외")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void increaseStock_shouldFail_whenInvalid(Long qty) {
        ProductMetaModel meta = ProductMetaModel.builder()
                .productId(1L)
                .stock(10L)
                .likeCount(0L)
                .reviewCount(0L)
                .viewCount(0L)
                .build();

        CoreException exception = assertThrows(CoreException.class, () -> {
            meta.increaseStock(qty);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고 차감: 0 이하로 차감 시 예외")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void decreaseStock_shouldFail_whenInvalid(Long qty) {
        ProductMetaModel meta = ProductMetaModel.builder()
                .productId(1L)
                .stock(10L)
                .likeCount(0L)
                .reviewCount(0L)
                .viewCount(0L)
                .build();

        CoreException exception = assertThrows(CoreException.class, () -> {
            meta.decreaseStock(qty);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고 차감시 부족하면 예외 발생")
    @Test
    void stock_decrease_fail_when_not_enough() {
        ProductMetaModel meta = new ProductMetaModel(1L, 2L, 0L, 0L, 0L);

        CoreException ex = assertThrows(CoreException.class, () -> meta.decreaseStock(3L));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("좋아요/리뷰/조회수 증가/감소 정상 동작")
    @Test
    void like_review_view_increase_and_decrease() {
        ProductMetaModel meta = new ProductMetaModel(1L, 0L, 0L, 0L, 0L);

        meta.increaseLike();
        assertThat(meta.getLikeCount()).isEqualTo(1L);
        meta.decreaseLike();
        assertThat(meta.getLikeCount()).isEqualTo(0L);

        meta.increaseReview();
        assertThat(meta.getReviewCount()).isEqualTo(1L);
        meta.decreaseReview();
        assertThat(meta.getReviewCount()).isEqualTo(0L);

        meta.increaseView();
        assertThat(meta.getViewCount()).isEqualTo(1L);
    }

    @DisplayName("좋아요/리뷰/조회수는 0 미만으로 내려가지 않는다")
    @Test
    void like_review_view_not_negative() {
        ProductMetaModel meta = new ProductMetaModel(1L, 0L, 0L, 0L, 0L);

        meta.decreaseLike();
        assertThat(meta.getLikeCount()).isEqualTo(0L);

        meta.decreaseReview();
        assertThat(meta.getReviewCount()).isEqualTo(0L);
    }
}

