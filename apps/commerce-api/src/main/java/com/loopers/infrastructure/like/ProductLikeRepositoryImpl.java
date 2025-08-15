package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeModel;
import com.loopers.domain.like.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public void save(ProductLikeModel like) {
        productLikeJpaRepository.save(like);
    }

    @Override
    public void delete(Long userId, Long productId) {
        productLikeJpaRepository.deleteByUserIdAndProductId(userId,productId);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return productLikeJpaRepository.existsByUserIdAndProductId( userId, productId );
    }

    @Override
    public long countByProductId(Long productId) {
        return productLikeJpaRepository.countByProductId( productId );
    }

    @Override
    public List<ProductLikeModel> findProductIdByUserId(Long userId) {
        return productLikeJpaRepository.findProductIdByUserId( userId );
    }

    @Override
    public List<Long> findByUserIdAndProductId(Long productId) {
        return productLikeJpaRepository.findUserByProductId( productId );
    }
}
