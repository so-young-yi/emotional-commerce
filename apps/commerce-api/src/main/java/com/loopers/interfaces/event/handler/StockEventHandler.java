package com.loopers.interfaces.event.handler;

import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.ProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class StockEventHandler {

    private final OrderService orderService;
    private final ProductStockService productStockService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
}
