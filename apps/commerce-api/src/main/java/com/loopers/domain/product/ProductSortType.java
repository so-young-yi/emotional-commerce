package com.loopers.domain.product;

import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

@Getter
public enum ProductSortType {
    LATEST("latest", "id", Sort.Direction.DESC),
    PRICE_ASC("price_asc", "sellPrice", Sort.Direction.ASC),
    PRICE_DESC("price_desc", "sellPrice", Sort.Direction.DESC),
    LIKES_DESC("likes_desc", "likeCount", Sort.Direction.DESC),
    STOCK_DESC("stock_desc", "stock", Sort.Direction.DESC),
    REVIEW_DESC("review_desc", "reviewCount", Sort.Direction.DESC);

    private final String value;
    private final String sortField;
    private final Sort.Direction direction;

    ProductSortType(String value, String sortField, Sort.Direction direction) {
        this.value = value;
        this.sortField = sortField;
        this.direction = direction;
    }

    public static ProductSortType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(LATEST);
    }
}
