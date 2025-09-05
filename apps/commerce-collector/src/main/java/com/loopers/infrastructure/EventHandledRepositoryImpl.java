package com.loopers.infrastructure;

import com.loopers.collector.domain.common.EventHandled;
import com.loopers.collector.domain.common.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventHandledRepositoryImpl implements EventHandledRepository {

    private final SpringDataEventHandledRepository jpaRepository;

    @Override
    public void save(EventHandled eventHandled) {
        jpaRepository.save(eventHandled);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

}
interface SpringDataEventHandledRepository extends JpaRepository<EventHandled, String> {
    boolean existsByEventId(String eventId);
}
