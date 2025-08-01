package com.loopers.domain.like;


import java.util.List;

public interface ProductLikeRepository {

    void save(ProductLikeModel like);

    void delete(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId );

    //상품의 좋아요 수
    long countByProductId(Long productId);

    //사용자가 좋아요한 상품 목록
    List<ProductLikeModel> findProductIdByUserId(Long userId );

    //특정상품을 좋아요한 사용자 목록
    List<Long> findByUserIdAndProductId(Long productId );

}
