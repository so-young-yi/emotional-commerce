package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductStockModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductStockJpaRepository extends JpaRepository<ProductStockModel, Long> {

    Optional<ProductStockModel> findByProductId(Long productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductStockModel s where s.productId = :productId")
    Optional<ProductStockModel> findByProductIdForUpdate(@Param("productId") Long productId);
    List<ProductStockModel> findByProductIdIn(List<Long> productIds);
}
