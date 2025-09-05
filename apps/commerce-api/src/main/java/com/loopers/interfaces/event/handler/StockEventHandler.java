package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.ProductStockService;
import com.loopers.interfaces.event.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class StockEventHandler {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final ProductStockService productStockService;

    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.payment().getStatus() != PaymentStatus.PAID) return;
        OrderModel order = orderService.getOrderDetail(event.orderId());
        for (OrderItemModel item : order.getOrderItems()) {
            try {
                productStockService.decreaseStockWithLock(item.getProductId(), item.getQuantity());
            } catch (Exception ex) {
                // 멱등/재시도 설계
            }
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "collector")
    @Transactional
    public void consume(String message) throws Exception {
        KafkaMessage<?> wrapper = objectMapper.readValue(message, new TypeReference<KafkaMessage<Map<String, Object>>>() {});
        if ("PaymentCompletedEvent".equals(wrapper.type())) {
            PaymentCompletedEvent event = objectMapper.convertValue(wrapper.payload(), PaymentCompletedEvent.class);
            onPaymentCompleted( event );
        }
    }

}
