package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductDetailProjection;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Optional<ProductModel> findById( Long id ) {
        return productJpaRepository.findById( id );
    }

    @Override
    public ProductModel save( ProductModel product ) {
        return productJpaRepository.save( product );
    }

    @Override
    public Page<ProductModel> findByBrandId(Long brandId, Pageable pageable) {
        return productJpaRepository.findByBrandId( brandId, pageable );
    }

    @Override
    public Page<ProductModel> findAll(Pageable pageable) {
        return productJpaRepository.findAll( pageable );
    }

    @Override
    public Page<ProductSummaryProjection> findProductSummaries(Long brandId, Pageable pageable) {
        return productJpaRepository.findProductSummaries(brandId, pageable);
    }

    @Override
    public Optional<ProductDetailProjection> findProductDetailById(Long productId) {
        return productJpaRepository.findProductDetailById(productId);
    }
}
