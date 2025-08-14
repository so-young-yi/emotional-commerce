package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "product_meta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class ProductMetaModel {

    @Id
    @EqualsAndHashCode.Include
    private Long productId;

    private Long likeCount;
    private Long reviewCount;
    private Long viewCount;

    public ProductMetaModel(Long productId, Long likeCount, Long reviewCount, Long viewCount) {
        if (productId == null || productId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        this.productId = productId;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
        this.viewCount = viewCount != null ? viewCount : 0L;
    }

    public void increaseLike() {
        this.likeCount += 1;
    }
    public void decreaseLike() {
        if (this.likeCount > 0) this.likeCount -= 1;
    }

    public void increaseReview() {
        this.reviewCount += 1;
    }
    public void decreaseReview() {
        if (this.reviewCount > 0) this.reviewCount -= 1;
    }

    public void increaseView() {
        this.viewCount += 1;
    }
}
