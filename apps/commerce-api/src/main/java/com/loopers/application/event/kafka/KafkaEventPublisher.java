package com.loopers.application.event.kafka;

import com.loopers.application.event.*;
import com.loopers.event.*;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.event.PaymentCompletedEvent;
import com.loopers.event.PaymentFailedEvent;
import com.loopers.event.ProductLikedEvent;
import com.loopers.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(Object event) {
        try {
            EventMessage eventMessage = convertToEventMessage(event);
            String topic = resolveTopic(event);
            kafkaTemplate.send(topic, eventMessage);
        } catch (Exception e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }

    private EventMessage convertToEventMessage(Object event) {
        if (event instanceof com.loopers.application.event.OrderCreatedEvent orderEvent) {
            List<OrderCreatedEvent.OrderItem> items = orderEvent.order().getOrderItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItem(
                    item.getProductId().toString(),
                    Math.toIntExact(item.getQuantity()),
                    item.getPriceSnapshot()
                ))
                .toList();
            return new OrderCreatedEvent(
                orderEvent.userId().toString(),
                orderEvent.orderId().toString(),
                items,
                orderEvent.order().getTotalAmount()
            );
        }
        if (event instanceof com.loopers.application.event.ProductLikedEvent likeEvent) {
            return new ProductLikedEvent(
                likeEvent.userId().toString(),
                likeEvent.productId().toString()
            );
        }
        if (event instanceof com.loopers.application.event.ProductUnlikedEvent unlikeEvent) {
            return new ProductUnlikedEvent(
                unlikeEvent.userId().toString(),
                unlikeEvent.productId().toString()
            );
        }
        if (event instanceof com.loopers.application.event.PaymentCompletedEvent paymentEvent) {
            return new PaymentCompletedEvent(
                paymentEvent.userId().toString(),
                paymentEvent.orderId().toString(),
                paymentEvent.payment().getId().toString(),
                paymentEvent.payment().getAmount()
            );
        }
        if (event instanceof com.loopers.application.event.PaymentFailedEvent failedEvent) {
            return new PaymentFailedEvent(
                failedEvent.userId().toString(),
                failedEvent.orderId().toString(),
                failedEvent.reason()
            );
        }
        throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
    }

    private String resolveTopic( Object event ) {
        if (event instanceof OrderCreatedEvent) return "order-events";
        if (event instanceof PaymentCompletedEvent || event instanceof PaymentFailedEvent) return "payment-events";
        if (event instanceof ProductLikedEvent || event instanceof ProductUnlikedEvent) return "product-events";
        return "default-events";
    }
}
