package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.PgType;
import com.loopers.infrastructure.external.pg.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentScheduler {

    private final PaymentService paymentService;
    private final Map<PgType, PaymentGateway> gatewayMap;

    // 1분마다 PENDING 결제 상태 확인
    @Scheduled(fixedDelay = 60000)
    public void checkPendingPayments() {
        // PENDING 상태 결제 모두 조회
        List<PaymentModel> pendings = paymentService.getPaymentsByStatus(PaymentStatus.PENDING);
        for (PaymentModel payment : pendings) {
            try {
                PgType pgType = payment.getPgType();
                PaymentGateway gateway = gatewayMap.get(pgType);
                if (gateway == null) {
                    log.warn("PG Gateway 없음: {}", pgType);
                    continue;
                }
                // 트랜잭션키로 PG에 상태 조회
                PaymentStatus status = gateway.checkPaymentStatus(payment.getTransactionKey());
                if (status != PaymentStatus.PENDING && status != payment.getStatus()) {
                    paymentService.updatePaymentStatus(payment.getOrderId(), status, null);
                    log.info("결제 상태 업데이트: orderId={}, old={}, new={}", payment.getOrderId(), payment.getStatus(), status);
                }
            } catch (Exception e) {
                log.warn("PG 상태 확인 실패: orderId={}, error={}", payment.getOrderId(), e.getMessage());
            }
        }
    }
}
