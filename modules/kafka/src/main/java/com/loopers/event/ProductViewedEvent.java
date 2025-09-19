package com.loopers.event;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonTypeName("ProductViewedEvent")
public class ProductViewedEvent extends EventMessage {
    private String productId;

    public ProductViewedEvent() {}

    public ProductViewedEvent(String userId, String productId) {
        super(
                UUID.randomUUID().toString(),
                "ProductViewedEvent",
                userId,
                Instant.now(),
                Map.of()
        );
        this.productId = productId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}