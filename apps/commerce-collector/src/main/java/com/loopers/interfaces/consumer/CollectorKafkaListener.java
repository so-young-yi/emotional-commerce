package com.loopers.interfaces.consumer;

import com.loopers.collector.application.ranking.ProductRankingService;
import com.loopers.collector.domain.event.EventHandled;
import com.loopers.collector.domain.event.EventHandledRepository;
import com.loopers.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CollectorKafkaListener {
    private final EventHandledRepository eventHandledRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductRankingService productRankingService;

    @KafkaListener(topics = "order-events", groupId = "collector-group")
    public void onOrderEvent(ConsumerRecord<String, EventMessage> record, Acknowledgment ack) {
        processEvent(record, ack);
    }

    @KafkaListener(topics = "product-events", groupId = "collector-group")
    public void onProductEvent(ConsumerRecord<String, EventMessage> record, Acknowledgment ack) {
        processEvent(record, ack);
    }

    @KafkaListener(topics = "payment-events", groupId = "collector-group")
    public void onPaymentEvent(ConsumerRecord<String, EventMessage> record, Acknowledgment ack) {
        processEvent(record, ack);
    }

    private void processEvent(ConsumerRecord<String, EventMessage> record, Acknowledgment ack) {
        try {
            EventMessage event = record.value();

            // 멱등성 체크
            if (eventHandledRepository.existsByEventId(event.getEventId())) {
                log.info("Duplicate event ignored: {}", event.getEventId());
                ack.acknowledge();
                return;
            }

            // 이벤트 처리
            handleEvent(event);

            // 처리 완료 기록
            eventHandledRepository.save(new EventHandled(event.getEventId()));
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage());
            sendToDLQ(record, e);
        }
    }

    private void handleEvent(EventMessage event) {
        log.info("Processing {} event: {}", event.getType(), event.getEventId());

        try {
            switch (event) {
                case ProductViewedEvent productViewed -> {
                    log.info("Handling ProductViewedEvent for product: {}", productViewed.getProductId());
                    productRankingService.updateProductView(productViewed.getProductId());
                }
                case ProductLikedEvent productLiked -> {
                    log.info("Handling ProductLikedEvent for product: {}", productLiked.getProductId());
                    productRankingService.updateProductLike(productLiked.getProductId());
                }
                case ProductUnlikedEvent productUnliked -> {
                    log.info("Product unliked: {}", productUnliked.getProductId());
                }
                case OrderCreatedEvent orderCreated -> {
                    log.info("Handling OrderCreatedEvent for order: {}", orderCreated.getOrderId());
                    handleOrderCreatedEvent(orderCreated);
                }
                case PaymentCompletedEvent paymentCompleted -> {
                    log.info("Payment completed for order: {}, amount: {}",
                        paymentCompleted.getOrderId(), paymentCompleted.getAmount());
                    // 결제 완료 이벤트 처리 로직이 여기 들어갈 수 있음
                }
                case PaymentFailedEvent paymentFailed -> {
                    log.warn("Payment failed for order: {} - {}", paymentFailed.getOrderId(), paymentFailed.getReason());
                }
                default -> log.warn("Unknown event type: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Failed to handle {} event: {}", event.getType(), e.getMessage());
            throw e; // Re-throw to trigger DLQ
        }
    }

    private void handleOrderCreatedEvent(OrderCreatedEvent orderCreated) {
        // 주문의 각 상품에 대해 랭킹 업데이트
        for (OrderCreatedEvent.OrderItem item : orderCreated.getItems()) {
            productRankingService.updateProductOrder(
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
            );
        }
        log.info("Order event processed for ranking: orderId={}, items={}",
                orderCreated.getOrderId(), orderCreated.getItems().size());
    }

    private void sendToDLQ(ConsumerRecord<String, EventMessage> record, Exception e) {
        try {
            kafkaTemplate.send("user-events-dlq", record.value());
            log.warn("Sent to DLQ - topic: {}, partition: {}, offset: {}, error: {}",
                    record.topic(), record.partition(), record.offset(), e.getMessage());
        } catch (Exception dlqException) {
            log.error("Failed to send to DLQ: {}", dlqException.getMessage());
        }
    }
}
