package com.loopers.interfaces.event.handler;

import com.loopers.application.event.OrderCreatedEvent;
import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductLiked(ProductLikedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductUnliked(ProductUnlikedEvent event) {
        log.info("외부 데이터 플랫폼 전송/로깅/메트릭 = {}", event.toString());
    }
}
