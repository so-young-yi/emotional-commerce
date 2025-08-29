package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentScheduler {

    private final PaymentService paymentService;

    // 1분마다 PENDING 결제 상태 확인
    @Scheduled(fixedDelay = 60000)
    public void checkPendingPayments() {
        paymentService.checkPendingPayments();
    }
}
