package com.loopers.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderCreatedEvent extends EventMessage {
    private String orderId;
    private List<OrderItem> items;
    private long totalAmount;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String userId, String orderId, List<OrderItem> items, long totalAmount) {
        super(
                UUID.randomUUID().toString(),
                "OrderCreated",
                userId,
                Instant.now(),
                Map.of()
        );
        this.orderId = orderId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public static class OrderItem {
        private String productId;
        private int quantity;
        private long price;

        public OrderItem() {}

        public OrderItem(String productId, int quantity, long price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public long getPrice() { return price; }
        public void setPrice(long price) { this.price = price; }
    }
}
