package com.loopers.application.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductMetaModel;

public record ProductInfo(
        Long id,
        Long brandId,
        String name,
        String description,
        Long price,
        Long stock,
        String status
) {
    public static ProductInfo from(ProductModel model, ProductMetaModel meta) {
        return new ProductInfo(
                model.getId(),
                model.getBrandId(),
                model.getName(),
                model.getDescription(),
                model.getSellPrice().getAmount(),
                meta != null ? meta.getStock() : 0L, // 메타가 없으면 0L로 처리
                model.getStatus().name()
        );
    }
}
