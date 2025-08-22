package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.point.UserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("POINT")
public class PointPaymentProcessor implements PaymentProcessor<Payment.PointRequest, Payment.PointResponse> {

    private final UserPointService userPointService;
    private final PaymentService paymentService;

    @Override
    public Payment.PointResponse process(Payment.PointRequest request) {
        PaymentResult result;
        try {
            userPointService.useUserPointWithLock(request.getUserId(), request.getAmount());
            result = new PaymentResult(PaymentStatus.PAID, null, null);
        } catch (Exception e) {
            result = new PaymentResult(PaymentStatus.FAILED, e.getMessage(), null);
        }
        paymentService.recordResult(request.getOrderId(), result);
        return result.toPointResponse();
    }
}
