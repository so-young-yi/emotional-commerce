package com.loopers.application.event.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.*;
import com.loopers.interfaces.event.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent( Object event ) {
        try {
            KafkaMessage<Object> message = KafkaMessage.of(event, resolveTopic(event));
            String json = objectMapper.writeValueAsString( message );
            kafkaTemplate.send( resolveTopic(event), json );

        } catch (Exception e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }

    private String resolveTopic( Object event ) {
        if (event instanceof OrderCreatedEvent) return "order-events";
        if (event instanceof PaymentCompletedEvent || event instanceof PaymentFailedEvent) return "payment-events";
        if (event instanceof ProductLikedEvent || event instanceof ProductUnlikedEvent) return "product-events";
        return "default-events";
    }
}
