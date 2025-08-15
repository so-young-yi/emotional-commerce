package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@Table(name = "product")
@Entity
@Getter
public class ProductModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long brandId;
    private String name;
    private String description;
    @Embedded
    private Money sellPrice;
    private Long stock;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    @Column(nullable = false)
    private ZonedDateTime sellAt; // 판매 시작일시

    protected ProductModel() {}

    public ProductModel(Long id, Long brandId, String name, String description, Money sellPrice, Long stock, ProductStatus status) {
        this(id, brandId, name, description, sellPrice, stock, status, ZonedDateTime.now());
    }

    public ProductModel(Long id, Long brandId, String name, String description, Money sellPrice, Long stock, ProductStatus status, ZonedDateTime sellAt ) {
        if (brandId == null || brandId <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "브랜드는 필수입니다.");
        if (name == null || name.isBlank()) throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        if (name.length() > 100) throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 100자 이내여야 합니다.");
        if (sellPrice == null) throw new CoreException(ErrorType.BAD_REQUEST, "상품가격은 필수입니다.");
        if (sellPrice.getAmount() <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "상품가격은 0보다 커야합니다.");
        if (stock == null || stock < 0) throw new CoreException(ErrorType.BAD_REQUEST, "재고는 양수값을 가집니다.");
        if (status == null) throw new CoreException(ErrorType.BAD_REQUEST, "상품의 상태값은 필수입니다.");
        if (status.isOrderable() && stock == 0) throw new CoreException(ErrorType.BAD_REQUEST, "판매중인 상품의 재고는 0개 이상이어야 합니다.");
        if (sellAt == null) throw new CoreException(ErrorType.BAD_REQUEST, "판매 시작일시는 필수입니다.");

        this.id = id;
        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.status = status;
        this.sellAt = sellAt;
    }


    public boolean isOrderable() {
        return status.isOrderable() && stock > 0;
    }

    public void decreaseStock(Long qty) {
        if (qty <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 1개 이상이어야 합니다.");
        if (!isOrderable()) throw new CoreException(ErrorType.BAD_REQUEST, "주문 불가");
        if (stock < qty) throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족");
        this.stock -= qty;
    }
}
