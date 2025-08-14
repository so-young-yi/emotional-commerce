package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductStockRepository extends org.springframework.data.repository.Repository<ProductStockModel, Long> {
    Optional<ProductStockModel> findByProductId(Long productId);
    Optional<ProductStockModel> findByProductIdForUpdate(Long productId); // 락용
    ProductStockModel save(ProductStockModel stock);
    List<ProductStockModel> findByProductIdIn(List<Long> productIds);
}
