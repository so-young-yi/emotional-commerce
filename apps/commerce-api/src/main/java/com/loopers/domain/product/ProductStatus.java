package com.loopers.domain.product;

public enum ProductStatus {
    ON_SALE,      // 판매중
    SOLD_OUT,     // 품절
    STOPPED;      // 판매중단

    public boolean isOrderable() {
        return this == ON_SALE;
    }
}
