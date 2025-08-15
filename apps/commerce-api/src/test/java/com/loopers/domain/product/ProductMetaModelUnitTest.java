package com.loopers.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProductMetaModelUnitTest {

    @DisplayName("좋아요/리뷰/조회수 증가/감소 정상 동작")
    @Test
    void like_review_view_increase_and_decrease() {
        ProductMetaModel meta = new ProductMetaModel(1L, 0L, 0L, 0L);

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
        ProductMetaModel meta = new ProductMetaModel(1L, 0L, 0L, 0L);

        meta.decreaseLike();
        assertThat(meta.getLikeCount()).isEqualTo(0L);

        meta.decreaseReview();
        assertThat(meta.getReviewCount()).isEqualTo(0L);
    }
}
