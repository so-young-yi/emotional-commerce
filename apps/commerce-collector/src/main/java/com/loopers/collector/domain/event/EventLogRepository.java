package com.loopers.collector.domain.event;


public interface EventLogRepository {

    void save(EventLog eventLog);
    boolean existsByEventId(String eventId);
}
