package com.loopers.interfaces.event.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        String type,
        String version,
        ZonedDateTime publishedAt,
        T payload
) {
    public static <T> KafkaMessage<T> of(T payload, String type) {
        return new KafkaMessage<>(
                UUID.randomUUID().toString(),
                type,
                "v1",
                ZonedDateTime.now(),
                payload
        );
    }
}
