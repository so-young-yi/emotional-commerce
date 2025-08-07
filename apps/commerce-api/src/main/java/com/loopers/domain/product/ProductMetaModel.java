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

    private Long stock;
    private Long likeCount;
    private Long reviewCount;
    private Long viewCount;

    public ProductMetaModel(Long productId, Long stock, Long likeCount, Long reviewCount, Long viewCount) {
        if (productId == null || productId <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        if (stock != null && stock < 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
        this.productId = productId;
        this.stock = stock != null ? stock : 0L;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
        this.viewCount = viewCount != null ? viewCount : 0L;
    }

    public void increaseStock(long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "증가 수량은 1 이상이어야 합니다.");
        this.stock += qty;
    }
    public void decreaseStock(long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 1 이상이어야 합니다.");
        if (this.stock < qty) throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족");
        this.stock -= qty;
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
