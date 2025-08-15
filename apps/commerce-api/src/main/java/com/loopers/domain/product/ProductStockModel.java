package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "product_stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductStockModel {

    @Id
    private Long productId;

    private Long stock;

    public void increaseStock(long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "증가 수량은 1 이상이어야 합니다.");
        this.stock += qty;
    }

    public void decreaseStock(long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 1 이상이어야 합니다.");
        if (this.stock < qty) throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족");
        this.stock -= qty;
    }
}
