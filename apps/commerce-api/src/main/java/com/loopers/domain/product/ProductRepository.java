package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {

    Optional<ProductModel> findById( Long id );
    ProductModel save( ProductModel product );
    Page<ProductModel> findByBrandId( Long brandId, Pageable pageable );
    Page<ProductModel> findAll( Pageable pageable );
}
