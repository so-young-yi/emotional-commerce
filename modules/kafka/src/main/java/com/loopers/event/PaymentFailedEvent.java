package com.loopers.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PaymentFailedEvent extends EventMessage {
    private String orderId;
    private String reason;

    public PaymentFailedEvent() {}

    public PaymentFailedEvent(String userId, String orderId, String reason) {
        super(
                UUID.randomUUID().toString(),
                "PaymentFailed",
                userId,
                Instant.now(),
                Map.of()
        );
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
