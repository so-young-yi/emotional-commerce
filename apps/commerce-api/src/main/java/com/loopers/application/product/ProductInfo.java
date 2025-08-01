package com.loopers.application.product;

import com.loopers.domain.product.ProductModel;

public record ProductInfo(
        Long id,
        Long brandId,
        String name,
        String description,
        Long price,
        Long stock,
        String status
) {
    public static ProductInfo from(ProductModel model) {
        return new ProductInfo(
                model.getId(),
                model.getBrandId(),
                model.getName(),
                model.getDescription(),
                model.getSellPrice().getAmount(),
                model.getStock(),
                model.getStatus().name()
        );
    }
}
