package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PaymentMethod;
import com.loopers.infrastructure.external.pg.PgClientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final Map<PaymentMethod, PaymentProcessor<?,?>> processorMap;

    public Payment.Response pay(Payment.Request request) {
        PaymentMethod method = request.getPaymentMethod();
        switch (method) {
            case CARD -> {
                PaymentProcessor<Payment.CardRequest, Payment.CardResponse> processor =
                        (PaymentProcessor<Payment.CardRequest, Payment.CardResponse>) processorMap.get(method);
                return processor.process((Payment.CardRequest) request);
            }
            case POINT -> {
                PaymentProcessor<Payment.PointRequest, Payment.PointResponse> processor =
                        (PaymentProcessor<Payment.PointRequest, Payment.PointResponse>) processorMap.get(method);
                return processor.process((Payment.PointRequest) request);
            }
            // 현금 등 추가
            default -> throw new IllegalArgumentException("지원하지 않는 결제 방식: " + method);
        }
    }

    public PaymentInfo getPayment(Long paymentId) {
        PaymentModel payment = paymentService.getPayment(paymentId);
        return PaymentInfo.from(payment);
    }

    public PaymentInfo getPaymentByOrderId(Long orderId) {
        PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) return null;
        return PaymentInfo.from(payment);
    }

    public void refund(Long paymentId) {
        paymentService.refund(paymentId);
    }

    public void handlePgCallback(PgClientDto.PgResponse pgResponse) {
        PaymentResult result = PaymentResult.fromPgResponse(pgResponse);
        paymentService.recordResult(Long.valueOf(pgResponse.orderId()), result);
    }

}
