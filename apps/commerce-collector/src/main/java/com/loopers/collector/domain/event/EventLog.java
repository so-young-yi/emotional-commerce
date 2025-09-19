package com.loopers.collector.domain.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "event_log")
@NoArgsConstructor
public class EventLog {

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "received_at", nullable = false)
    private ZonedDateTime receivedAt = ZonedDateTime.now();

    public EventLog(String eventId, String eventType, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public static EventLog of(String eventId, String eventType, String payload) {
        return new EventLog(eventId, eventType, payload);
    }
}
