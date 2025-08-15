package com.loopers.application.product;

import com.loopers.domain.product.ProductSummaryProjection;

import java.io.Serializable;

public record ProductSummaryInfo(
        Long id,
        Long brandId,
        String name,
        String description,
        Long sellPrice,
        String status,
        Long likeCount,
        Long reviewCount,
        Long viewCount,
        Long stock
) implements Serializable {
    public static ProductSummaryInfo from(ProductSummaryProjection p) {
        return new ProductSummaryInfo(
                p.getId(),
                p.getBrandId(),
                p.getName(),
                p.getDescription(),
                p.getSellPrice(),
                p.getStatus(),
                p.getLikeCount(),
                p.getReviewCount(),
                p.getViewCount(),
                p.getStock()
        );
    }
}
