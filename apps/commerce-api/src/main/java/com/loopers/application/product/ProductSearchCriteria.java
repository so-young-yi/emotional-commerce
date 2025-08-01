package com.loopers.application.product;

import com.loopers.interfaces.api.product.ProductV1Dto;

public record ProductSearchCriteria(
        Long brandId,
        String sort,
        Integer page,
        Integer size
) {
    public int getPageOrDefault() {
        return page != null ? page : 0;
    }

    public int getSizeOrDefault() {
        return size != null ? size : 20;
    }

    public String getSortOrDefault() {
        return sort != null ? sort : "latest";
    }

    public static ProductSearchCriteria from(ProductV1Dto.ProductRequest request) {
        return new ProductSearchCriteria(
                request.brandId(),
                request.sort(),
                request.page(),
                request.size()
        );
    }
}
