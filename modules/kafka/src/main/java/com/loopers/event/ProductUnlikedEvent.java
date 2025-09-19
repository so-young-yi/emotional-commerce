package com.loopers.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ProductUnlikedEvent extends EventMessage {
    private String productId;

    public ProductUnlikedEvent() {}

    public ProductUnlikedEvent(String userId, String productId) {
        super(
                UUID.randomUUID().toString(),
                "ProductUnliked",
                userId,
                Instant.now(),
                Map.of()
        );
        this.productId = productId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}