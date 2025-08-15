package com.loopers.domain.like;

import com.loopers.application.like.ProductLikeInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public boolean likeProduct(ProductLikeInfo productLikeInfo){
        boolean alreadyLiked = isProductLiked(productLikeInfo);
        if(!alreadyLiked){
            productLikeRepository.save(productLikeInfo.toModel());
        }
        return true;
    };

    @Transactional
    public boolean unlikeProduct(ProductLikeInfo productLikeInfo) {
        productLikeRepository.delete(productLikeInfo.userId(), productLikeInfo.productId());
        return false;
    }

    // 상품의 좋아요여부
    public boolean isProductLiked(ProductLikeInfo productLikeInfo) {
        return productLikeRepository.existsByUserIdAndProductId( productLikeInfo.userId(), productLikeInfo.productId() );
    }

    // 상품의 좋아요 수
    public long getLikeCountOfProduct(Long productId){
        return productLikeRepository.countByProductId( productId );
    };


    // 여러 상품의 좋아요 수를 한 번에 조회
    public Map<Long, Long> getLikeCountsForProductIds(List<Long> productIds) {
        Map<Long, Long> result = new HashMap<>();
        for (Long productId : productIds) {
            long count = productLikeRepository.countByProductId(productId);
            result.put(productId, count);
        }
        return result;
    }


}
