package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.OrderCreatedEvent;
import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
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
public class AnalyticsHandler {

    private final ObjectMapper objectMapper;


    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @KafkaListener(topics = "payment-events", groupId = "collector")
    @Transactional
    public void paymentConsume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("PaymentCompletedEvent".equals(wrapper.type())) {
            PaymentCompletedEvent event = objectMapper.convertValue(wrapper.payload(), PaymentCompletedEvent.class);
            onPaymentCompleted( event );
        }
    }

    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @KafkaListener(topics = "order-events", groupId = "collector")
    @Transactional
    public void orderConsume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("OrderCreatedEvent".equals(wrapper.type())) {
            OrderCreatedEvent event = objectMapper.convertValue(wrapper.payload(), OrderCreatedEvent.class);
            onOrderCreated( event );
        }
    }

    public void onProductLiked(ProductLikedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    public void onProductUnliked(ProductUnlikedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @KafkaListener(topics = "product-events", groupId = "collector")
    @Transactional
    public void productLikeConsume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("ProductLikedEvent".equals(wrapper.type())) {
            ProductLikedEvent event = objectMapper.convertValue(wrapper.payload(), ProductLikedEvent.class);
            onProductLiked( event );
        } else if ("ProductUnlikedEvent".equals(wrapper.type())) {
            ProductUnlikedEvent event = objectMapper.convertValue(wrapper.payload(), ProductUnlikedEvent.class);
            onProductUnliked( event );
        }
    }




}
