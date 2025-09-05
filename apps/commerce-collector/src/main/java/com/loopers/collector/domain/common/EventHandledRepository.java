package com.loopers.collector.domain.common;

public interface EventHandledRepository {

    void save(EventHandled eventHandled);
    boolean existsByEventId(String eventId);
}
