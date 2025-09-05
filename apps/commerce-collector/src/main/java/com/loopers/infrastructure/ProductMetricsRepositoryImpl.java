package com.loopers.infrastructure;

import com.loopers.collector.domain.metrics.ProductMetrics;
import com.loopers.collector.domain.metrics.ProductMetricsId;
import com.loopers.collector.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final SpringDataProductMetricsRepository productMetricsJpaRepository;


    @Override
    public Optional<ProductMetrics> findById(ProductMetricsId id) {
        return productMetricsJpaRepository.findById(id);
    }

    @Override
    public void save(ProductMetrics metrics) {
        productMetricsJpaRepository.save(metrics);
    }

}
interface SpringDataProductMetricsRepository extends JpaRepository<ProductMetrics, ProductMetricsId> {
}
