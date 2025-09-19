package com.loopers.infrastructure;

import com.loopers.collector.domain.event.EventLog;
import com.loopers.collector.domain.event.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

    private final SpringDataEventLogRepository jpaRepository;

    @Override
    public void save(EventLog eventLog) {
        jpaRepository.save(eventLog);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

}
// 내부용 Spring Data JPA 인터페이스
interface SpringDataEventLogRepository extends JpaRepository<EventLog, String> {
    boolean existsByEventId(String eventId);
}
