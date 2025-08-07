package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetaModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductMetaJpaRepository extends JpaRepository<ProductMetaModel, Long> {

    Optional<ProductMetaModel> findByProductId(Long productId);
    List<ProductMetaModel> findByProductIdIn(List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pm from ProductMetaModel pm where pm.productId = :productId")
    Optional<ProductMetaModel> findByProductIdForUpdate(@Param("productId") Long productId);

}
