package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;

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
}
