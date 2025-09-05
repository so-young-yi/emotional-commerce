package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.interfaces.consumer.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CollectorKafkaListener {
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "catalog-events", groupId = "collector-group")
    public void onCatalogEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            KafkaMessage<?> event = objectMapper.readValue(
                    record.value(),
                    new com.fasterxml.jackson.core.type.TypeReference<KafkaMessage<?>>() {}
            );
            // ... CollectorService 호출 등
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("카탈로그 event = " + event);
            ack.acknowledge();
        } catch (Exception e) {
            // DLQ, 로깅 등
        }
    }
    @KafkaListener(topics = "order-events", groupId = "collector-group")
    public void onOrderEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            KafkaMessage<?> event = objectMapper.readValue(
                    record.value(),
                    new com.fasterxml.jackson.core.type.TypeReference<KafkaMessage<?>>() {}
            );
            // ... CollectorService 호출 등
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("오더 event = " + event);
            ack.acknowledge();
        } catch (Exception e) {
            // DLQ, 로깅 등
        }
    }
}
