package com.loopers.application.product;

import com.loopers.domain.product.ProductDetailProjection;
import com.loopers.domain.product.ProductSummaryProjection;

public record ProductInfo(
        Long id,
        Long brandId,
        String name,
        String description,
        Long price,
        Long stock,
        String status
) {
    public static ProductInfo from(ProductSummaryProjection p) {
        return new ProductInfo(
                p.getId(),
                p.getBrandId(),
                p.getName(),
                p.getDescription(),
                p.getSellPrice(),
                p.getStock(),
                p.getStatus()
        );
    }

    public static ProductInfo from(ProductDetailProjection p) {
        return new ProductInfo(
                p.getId(),
                p.getBrandId(),
                p.getName(),
                p.getDescription(),
                p.getSellPrice(),
                p.getStock(),
                p.getStatus()
        );
    }
}
