package com.loopers.infrastructure;

import com.loopers.collector.domain.event.EventHandled;
import com.loopers.collector.domain.event.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public int countByEventId(String eventId) {
        return jpaRepository.countByEventId(eventId);
    }

    @Override
    public List<EventHandled> findAll() {
        return jpaRepository.findAll();
    }

}
interface SpringDataEventHandledRepository extends JpaRepository<EventHandled, String> {
    boolean existsByEventId(String eventId);
    int countByEventId(String eventId);
}
