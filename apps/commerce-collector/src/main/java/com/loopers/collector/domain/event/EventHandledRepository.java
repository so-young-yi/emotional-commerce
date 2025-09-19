package com.loopers.collector.domain.event;

import java.util.List;

public interface EventHandledRepository {

    void save(EventHandled eventHandled);
    boolean existsByEventId(String eventId);
    int countByEventId(String eventId);
    List<EventHandled> findAll();
}
