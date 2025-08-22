package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component("PG_SIMULATOR")
public class PgSimulatorGateway implements PaymentGateway {

    private final PgClient pgClient;
    private final PaymentService paymentService;

    @Override
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "fallback")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment.CardResponse requestPayment(Payment.CardRequest request, String callbackUrl) {

        PgClientDto.PgRequest pgRequest = PgClientDto.PgRequest.from(request, callbackUrl);
        // PG 시뮬레이터에 결제 요청
        var apiResponse = pgClient.request(pgRequest);

        PaymentResult result;
        if (apiResponse == null || apiResponse.data() == null) {
            result = new PaymentResult(PaymentStatus.FAILED, "PG_COMMUNICATION_ERROR", null);
        } else {
            result = PaymentResult.fromPgResponse(apiResponse.data());
        }
        paymentService.recordResult(request.getOrderId(), result);
        return result.toCardResponse();
    }

    public Payment.CardResponse fallback(Payment.CardRequest request, String callbackUrl, Throwable t) {
        PaymentResult result = new PaymentResult(PaymentStatus.FAILED, "PG_UNAVAILABLE", null);
        paymentService.recordResult(request.getOrderId(), result);
        return result.toCardResponse();
    }

    @Override
    public PaymentStatus checkPaymentStatus(String transactionKey) {
        var apiResponse = pgClient.checkStatus(transactionKey);
        if (apiResponse == null || apiResponse.data() == null) {
            // PG 통신 실패 시
            return PaymentStatus.FAILED;
        }
        return PaymentResult.fromPgResponse(apiResponse.data()).status();
    }

    @Override
    public String getTransactionKey(Long orderId) {
        var apiResponse = pgClient.findByOrderId(orderId.toString());
        if (apiResponse == null || apiResponse.data() == null || apiResponse.data().isEmpty()) {
            return null;
        }
        return apiResponse.data().get(0).transactionKey();
    }
}
