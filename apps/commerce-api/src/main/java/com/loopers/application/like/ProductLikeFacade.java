package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.ProductMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductLikeService productLikeService;
    private final ProductMetaService productMetaService;

    @Transactional
    public boolean likeProduct(Long userId, Long productId) {
        boolean liked = productLikeService.likeProduct(new ProductLikeInfo(userId, productId));
        if (liked) {
            productMetaService.increaseLike(productId);
        }
        return liked;
    }

    @Transactional
    public boolean unlikeProduct(Long userId, Long productId) {
        boolean unliked = productLikeService.unlikeProduct(new ProductLikeInfo(userId, productId));
        if (unliked) {
            productMetaService.decreaseLike(productId);
        }
        return unliked;
    }

    public boolean isProductLiked(Long userId, Long productId) {
        return productLikeService.isProductLiked(new ProductLikeInfo(userId, productId));
    }

    public long getLikeCountOfProduct(Long productId) {
        return productLikeService.getLikeCountOfProduct(productId);
    }
}
