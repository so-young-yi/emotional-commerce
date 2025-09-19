package com.loopers.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PaymentCompletedEvent extends EventMessage {
    private String orderId;
    private String paymentId;
    private long amount;

    public PaymentCompletedEvent() {}

    public PaymentCompletedEvent(String userId, String orderId, String paymentId, long amount) {
        super(
                UUID.randomUUID().toString(),
                "PaymentCompleted",
                userId,
                Instant.now(),
                Map.of()
        );
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
}
