package com.loopers.event;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonTypeName("ProductLikedEvent")
public class ProductLikedEvent extends EventMessage {
    private String productId;

    public ProductLikedEvent() {}

    public ProductLikedEvent(String userId, String productId) {
        super(
                UUID.randomUUID().toString(),
                "ProductLikedEvent",
                userId,
                Instant.now(),
                Map.of()
        );
        this.productId = productId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}