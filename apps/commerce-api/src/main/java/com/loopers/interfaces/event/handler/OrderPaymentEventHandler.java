package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.PaymentFailedEvent;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentStatus;
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
public class OrderPaymentEventHandler {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.payment().getStatus() != PaymentStatus.PAID) return;
        OrderModel order = orderService.getOrderDetail(event.orderId());
        if (order.getStatus() == OrderStatus.ORDERED) {
            orderService.markOrderAsPaid(event.orderId());
        }
    }

    public void onPaymentFailed(PaymentFailedEvent event) {
        orderService.cancelOrder(event.orderId());
    }

    @KafkaListener(topics = "payment-events", groupId = "collector")
    @Transactional
    public void consume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("PaymentCompletedEvent".equals(wrapper.type())) {
            PaymentCompletedEvent event = objectMapper.convertValue(wrapper.payload(), PaymentCompletedEvent.class);
            onPaymentCompleted( event );
        } else if ("PaymentFailedEvent".equals(wrapper.type())) {
            PaymentFailedEvent event = objectMapper.convertValue(wrapper.payload(), PaymentFailedEvent.class);
            onPaymentFailed( event );
        }
    }

}
