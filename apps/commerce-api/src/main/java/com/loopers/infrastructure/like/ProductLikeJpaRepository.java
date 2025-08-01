package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLikeModel, Long> {

    boolean existsByUserIdAndProductId( Long userId, Long productId );

    void deleteByUserIdAndProductId( Long userId, Long productId );

    long countByProductId(Long productId);

    List<ProductLikeModel> findProductIdByUserId( Long userId );

    List<Long> findUserByProductId(Long productId );

}
