package com.loopers.interfaces.api.like;

import java.util.List;

public class ProductLikeV1Dto {

    public record ProductLikeResponse(
            Long userId,
            Long productId,
            Boolean isLike
    ) {}

    public record ProductListResponse(
            List<ProductLikeResponse> items
    ) {}
}
