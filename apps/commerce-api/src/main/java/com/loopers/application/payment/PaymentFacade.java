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

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final Map<PaymentMethod, PaymentProcessor<?, ?>> processorMap;

    @SuppressWarnings("unchecked")
    public <R extends Payment.Response> R pay(Payment.Request<R> request) {
        PaymentProcessor<Payment.Request<R>, R> processor =
                (PaymentProcessor<Payment.Request<R>, R>) processorMap.get(request.getPaymentMethod());
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }
        return processor.process(request);
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
