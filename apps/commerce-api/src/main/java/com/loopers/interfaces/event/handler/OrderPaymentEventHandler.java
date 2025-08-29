package com.loopers.interfaces.event.handler;

import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.PaymentFailedEvent;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentEventHandler {

    private final OrderService orderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.payment().getStatus() != PaymentStatus.PAID) return;
        OrderModel order = orderService.getOrderDetail(event.orderId());
        if (order.getStatus() == OrderStatus.ORDERED) {
            orderService.markOrderAsPaid(event.orderId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderService.cancelOrder(event.orderId());
        log.info("주문 자동 취소: orderId={}, reason={}", event.orderId(), event.reason());
    }
}
