package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.UserPointService;
import com.loopers.interfaces.event.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class PointEventHandler {

    private final ObjectMapper objectMapper;
    private final UserPointService userPointService;

    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.payment().getStatus() != PaymentStatus.PAID) return;
        try {
            if (event.userId() != null && event.payment().getAmount() != null) {
                userPointService.useUserPointWithLock(event.userId(), event.payment().getAmount());
            }
        } catch (Exception ex) {
            // 실패 시 로깅/재시도. 본 확정에는 영향 주지 않음.
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
