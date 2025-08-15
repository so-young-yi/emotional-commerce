package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductLikeService productLikeService;

    // 좋아요 등록 (멱등성 보장)
    public boolean likeProduct(Long userId, Long productId) {
        return productLikeService.likeProduct(new ProductLikeInfo(userId, productId));
    }

    // 좋아요 해제 (멱등성 보장)
    public boolean unlikeProduct(Long userId, Long productId) {
        return productLikeService.unlikeProduct(new ProductLikeInfo(userId, productId));
    }

    // 좋아요 여부 조회
    public boolean isProductLiked(Long userId, Long productId) {
        return productLikeService.isProductLiked(new ProductLikeInfo(userId, productId));
    }

    // 좋아요 수 조회
    public long getLikeCountOfProduct(Long productId) {
        return productLikeService.getLikeCountOfProduct(productId);
    }
}
