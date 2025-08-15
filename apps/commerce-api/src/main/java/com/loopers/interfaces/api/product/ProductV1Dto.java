package com.loopers.interfaces.api.product;

import java.util.List;

public class ProductV1Dto {

    public record ProductRequest(
            Long brandId,
            String sort,
            Integer page,
            Integer size
    ){ }

    public record ProductSummaryResponse(
            Long id,
            String name,
            String brandName,
            Long price,
            Long quantity,
            String status,
            Long likeCount
    ){

    }

    public record ProductListPageResponse(
            List<ProductSummaryResponse> items,
            long totalCount,
            int totalPages,
            int page,
            int size
    ) { }

    public record ProductDetailResponse(
            Long id,
            String name,
            String brandName,
            Long price,
            Long quantity,
            String status,
            long likeCount
    ){ }


}
