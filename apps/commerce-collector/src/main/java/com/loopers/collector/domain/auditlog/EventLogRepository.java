package com.loopers.collector.domain.auditlog;


public interface EventLogRepository {

    void save(EventLog eventLog);
    boolean existsByEventId(String eventId);
}
