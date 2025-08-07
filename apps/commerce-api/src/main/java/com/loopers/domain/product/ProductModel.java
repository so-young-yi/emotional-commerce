package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Table(name = "product")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Long brandId;
    private String name;
    private String description;

    @Embedded
    private Money sellPrice;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false)
    private ZonedDateTime sellAt;


    public ProductModel(Long brandId, String name, String description, Money sellPrice, ProductStatus status) {
        this(brandId, name, description, sellPrice, status, ZonedDateTime.now());
    }

    public ProductModel(Long brandId, String name, String description, Money sellPrice, ProductStatus status, ZonedDateTime sellAt) {
        if (brandId == null || brandId <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "브랜드는 필수입니다.");
        if (name == null || name.isBlank()) throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        if (name.length() > 100) throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 100자 이내여야 합니다.");
        if (sellPrice == null) throw new CoreException(ErrorType.BAD_REQUEST, "상품가격은 필수입니다.");
        if (sellPrice.getAmount() <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "상품가격은 0보다 커야합니다.");
        if (status == null) throw new CoreException(ErrorType.BAD_REQUEST, "상품의 상태값은 필수입니다.");
        if (sellAt == null) throw new CoreException(ErrorType.BAD_REQUEST, "판매 시작일시는 필수입니다.");

        this.id = id;
        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.sellPrice = sellPrice;
        this.status = status;
        this.sellAt = sellAt;
    }

}
