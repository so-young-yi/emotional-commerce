package com.loopers.application.product;

import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.product.ProductV1Dto;

public record ProductSearchCriteria(
        Long brandId,
        ProductSortType sortType,
        Integer page,
        Integer size
) {
    public int getPageOrDefault() { return page != null ? page : 0; }
    public int getSizeOrDefault() { return size != null ? size : 20; }
    public ProductSortType getSortTypeOrDefault() { return sortType != null ? sortType : ProductSortType.LATEST; }

    public static ProductSearchCriteria from(ProductV1Dto.ProductRequest request) {
        return new ProductSearchCriteria(
                request.brandId(),
                request.sort() != null ? ProductSortType.from(request.sort()) : ProductSortType.LATEST,
                request.page(),
                request.size()
        );
    }
}
