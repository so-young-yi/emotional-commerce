package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductMetaRepository {

    Optional<ProductMetaModel> findByProductId(Long productId);
    Optional<ProductMetaModel> findByProductIdForUpdate( Long productId );
    List<ProductMetaModel> findByProductIdIn(List<Long> productIds);
    ProductMetaModel save(ProductMetaModel meta);
}
