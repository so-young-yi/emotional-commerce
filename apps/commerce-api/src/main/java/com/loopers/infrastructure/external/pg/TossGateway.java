package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/* TODO 멀티 pg 로 추가된다면 */
@RequiredArgsConstructor
@Component("TOSS")
public class TossGateway implements PaymentGateway {

    @Override
    @CircuitBreaker(name = "tossCircuit", fallbackMethod = "fallback")
    public Payment.CardResponse requestPayment(Payment.CardRequest request, String callbackUrl) {
        // PG 시뮬레이터에 결제 요청
        return new Payment.CardResponse(PaymentStatus.PAID,"transactionKey","reason");
    }

    public Payment.CardResponse fallback(Payment.CardRequest request, Throwable t) {
        // fallback
        PaymentResult result = new PaymentResult(PaymentStatus.FAILED, "PG_UNAVAILABLE", null);
        return result.toCardResponse();
    }

    @Override
    public PaymentStatus checkPaymentStatus(String transactionKey) {
        // PG 시뮬레이터에 상태 조회
        return PaymentStatus.PENDING; // 예시
    }

    @Override
    public String getTransactionKey(Long orderId) {
        // 주문에 엮인 트랜잭션키 조회
        return "txKey";
    }
}
