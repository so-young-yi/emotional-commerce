package com.loopers.domain.product;

public interface ProductSummaryProjection {
    Long getId();
    Long getBrandId();
    String getName();
    String getDescription();
    Long getSellPrice();
    String getStatus();
    Long getLikeCount();
    Long getReviewCount();
    Long getViewCount();
    Long getStock();
}
