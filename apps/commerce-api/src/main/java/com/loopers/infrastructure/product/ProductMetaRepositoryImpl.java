package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetaModel;
import com.loopers.domain.product.ProductMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductMetaRepositoryImpl implements ProductMetaRepository {

    private final ProductMetaJpaRepository productMetaJpaRepository;

    @Override
    public Optional<ProductMetaModel> findByProductId(Long productId) {
        return productMetaJpaRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductMetaModel> findByProductIdForUpdate(Long productId) {
        return productMetaJpaRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public List<ProductMetaModel> findByProductIdIn(List<Long> productIds) {
        return productMetaJpaRepository.findByProductIdIn(productIds);
    }

    @Override
    public ProductMetaModel save(ProductMetaModel meta) {
        return productMetaJpaRepository.save(meta);
    }
}
