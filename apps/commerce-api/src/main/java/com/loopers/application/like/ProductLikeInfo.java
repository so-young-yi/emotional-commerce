package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record ProductLikeInfo(
        Long userId,
        Long productId
) {
    public ProductLikeInfo {
        if (userId == null || userId <= 0) throw new CoreException(ErrorType.BAD_REQUEST,"유효하지 않은 userId");
        if (productId == null || productId <= 0) throw new CoreException(ErrorType.BAD_REQUEST,"유효하지 않은 productId");
    }
    public static ProductLikeInfo from(ProductLikeModel productLikeModel) {
        return new ProductLikeInfo(
                productLikeModel.getUserId(),
                productLikeModel.getProductId()
        );
    }
    public ProductLikeModel toModel() {
        return new ProductLikeModel(
                userId,
                productId
        );
    }
}
