package com.loopers.interfaces.event.handler;

import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.UserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class PointEventHandler {

    private final UserPointService userPointService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
}
