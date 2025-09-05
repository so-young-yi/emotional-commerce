package com.loopers.collector.domain;

import com.loopers.collector.domain.auditlog.EventLog;
import com.loopers.collector.domain.auditlog.EventLogRepository;
import com.loopers.collector.domain.common.EventHandled;
import com.loopers.collector.domain.metrics.ProductMetrics;
import com.loopers.collector.domain.metrics.ProductMetricsId;
import com.loopers.collector.domain.metrics.ProductMetricsRepository;
import com.loopers.collector.domain.common.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CollectorService {

    private final EventLogRepository eventLogRepository;
    private final EventHandledRepository eventHandledRepository;
    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void handleEvent(String eventId, String eventType, String payload, Long productId, String metricType, long value) {
        // 멱등성 체크
        if (eventHandledRepository.existsByEventId(eventId)) return;

        // 감사 로그 저장
        eventLogRepository.save(EventLog.of(eventId, eventType, payload));

        // 집계 처리
        if (productId != null && metricType != null) {
            ProductMetricsId metricsId = new ProductMetricsId(productId, LocalDate.now());
            ProductMetrics metrics = productMetricsRepository.findById(metricsId)
                    .orElse(new ProductMetrics(productId, LocalDate.now()));
            switch (metricType) {
                case "like":
                    metrics.increaseLikeCount(value);
                    break;
                case "sales":
                    metrics.increaseSalesCount(value);
                    break;
                case "view":
                    metrics.increaseViewCount(value);
                    break;
            }
            productMetricsRepository.save(metrics);
        }

        // 멱등성 기록
        eventHandledRepository.save(EventHandled.of(eventId));
    }
}
