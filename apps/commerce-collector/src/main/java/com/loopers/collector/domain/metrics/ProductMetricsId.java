package com.loopers.collector.domain.metrics;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
public class ProductMetricsId implements Serializable {
    private Long productId;
    private LocalDate date;

    public ProductMetricsId(Long productId, LocalDate date) {
        this.productId = productId;
        this.date = date;
    }
}
