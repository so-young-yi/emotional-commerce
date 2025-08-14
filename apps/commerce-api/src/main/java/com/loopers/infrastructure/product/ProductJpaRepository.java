package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductDetailProjection;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    Page<ProductModel> findByBrandId(Long brandId, Pageable pageable);
    Page<ProductModel> findAll(Pageable pageable);

    @Query("""
        SELECT p.id AS id, p.brandId AS brandId, p.name AS name, p.description AS description,
               p.sellPrice.amount AS sellPrice, p.status AS status,
               m.likeCount AS likeCount, m.reviewCount AS reviewCount, m.viewCount AS viewCount,
               s.stock AS stock
        FROM ProductModel p
        LEFT JOIN ProductMetaModel m ON p.id = m.productId
        LEFT JOIN ProductStockModel s ON p.id = s.productId
        WHERE (:brandId IS NULL OR p.brandId = :brandId)
    """)
    Page<ProductSummaryProjection> findProductSummaries(Long brandId, Pageable pageable);

    @Query("""
        SELECT p.id AS id, p.brandId AS brandId, p.name AS name, p.description AS description,
               p.sellPrice.amount AS sellPrice, p.status AS status,
               m.likeCount AS likeCount, m.reviewCount AS reviewCount, m.viewCount AS viewCount,
               s.stock AS stock
        FROM ProductModel p
        LEFT JOIN ProductMetaModel m ON p.id = m.productId
        LEFT JOIN ProductStockModel s ON p.id = s.productId
        WHERE p.id = :productId
    """)
    Optional<ProductDetailProjection> findProductDetailById(Long productId);
}
