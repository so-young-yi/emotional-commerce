package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.OrderCreatedEvent;
import com.loopers.interfaces.event.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationHandler {

    private final ObjectMapper objectMapper;

    public void onOrderCreated(OrderCreatedEvent e) {
        log.info("주문알림 = {}", e);
    }

    @KafkaListener(topics = "order-events", groupId = "collector")
    @Transactional
    public void consume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("OrderCreatedEvent".equals(wrapper.type())) {
            OrderCreatedEvent event = objectMapper.convertValue(wrapper.payload(), OrderCreatedEvent.class);
            onOrderCreated( event );
        }
    }
}
