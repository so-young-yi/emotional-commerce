package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductStockModel;
import com.loopers.domain.product.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final ProductStockJpaRepository productStockJpaRepository;

    @Override
    public Optional<ProductStockModel> findByProductId(Long productId) {
        return productStockJpaRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductStockModel> findByProductIdForUpdate(Long productId) {
        return productStockJpaRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public ProductStockModel save(ProductStockModel stock) {
        return productStockJpaRepository.save(stock);
    }

    @Override
    public List<ProductStockModel> findByProductIdIn(List<Long> productIds) {
        return productStockJpaRepository.findByProductIdIn(productIds);
    }
}
