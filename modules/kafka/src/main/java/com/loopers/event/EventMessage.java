package com.loopers.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProductViewedEvent.class, name = "ProductViewedEvent"),
        @JsonSubTypes.Type(value = ProductLikedEvent.class, name = "ProductLikedEvent"),
        @JsonSubTypes.Type(value = ProductUnlikedEvent.class, name = "ProductUnlikedEvent"),
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "OrderCreatedEvent"),
        @JsonSubTypes.Type(value = PaymentCompletedEvent.class, name = "PaymentCompletedEvent"),
        @JsonSubTypes.Type(value = PaymentFailedEvent.class, name = "PaymentFailedEvent")
})
public abstract class EventMessage {
    private String eventId;
    private String type;
    private String userId;
    private Instant timestamp;
    private Map<String, Object> metadata;

    protected EventMessage() {}

    protected EventMessage(String eventId, String type, String userId, Instant timestamp, Map<String, Object> metadata) {
        this.eventId = eventId;
        this.type = type;
        this.userId = userId;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}