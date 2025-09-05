package com.loopers.collector.domain.metrics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/* 집계처리 */
@Getter
@Entity
@Table(name = "product_metrics")
@NoArgsConstructor
public class ProductMetrics {

    @EmbeddedId
    private ProductMetricsId id;

    @Column(name = "sales_count", nullable = false)
    private long salesCount = 0L;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;

    public ProductMetrics(Long productId, LocalDate date) {
        this.id = new ProductMetricsId(productId, date);
    }

    public void increaseLikeCount(long value) {
        this.likeCount += value;
    }
    public void increaseSalesCount(long value) {
        this.salesCount += value;
    }
    public void increaseViewCount(long value) {
        this.viewCount += value;
    }

    public void decreaseLikeCount(long value) {
        this.likeCount -= value;
    }
    public void decreaseSalesCount(long value) {
        this.salesCount -= value;
    }
    public void decreaseViewCount(long value) {
        this.viewCount -= value;
    }
}
