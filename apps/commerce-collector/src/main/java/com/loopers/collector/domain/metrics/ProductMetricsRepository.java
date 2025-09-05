package com.loopers.collector.domain.metrics;

import java.util.Optional;

public interface ProductMetricsRepository {

    Optional<ProductMetrics> findById(ProductMetricsId id);
    void save(ProductMetrics metrics);
}
