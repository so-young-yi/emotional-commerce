package com.loopers.application.like;

import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.ProductMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductLikeService productLikeService;
    private final ProductMetaService productMetaService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean likeProduct(Long userId, Long productId) {
        boolean liked = productLikeService.likeProduct(new ProductLikeInfo(userId, productId));
        if (liked) {
            productMetaService.increaseLike(productId);
            eventPublisher.publishEvent(new ProductLikedEvent(userId, productId));
        }
        return liked;
    }

    @Transactional
    public boolean unlikeProduct(Long userId, Long productId) {
        boolean unliked = productLikeService.unlikeProduct(new ProductLikeInfo(userId, productId));
        if (!unliked) {
            productMetaService.decreaseLike(productId);
            eventPublisher.publishEvent(new ProductUnlikedEvent(userId, productId));
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
