package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentInfo;

public class PaymentV1Dto {

    public record PaymentResponse(
            Long id,
            Long orderId,
            Long amount,
            String paidAt,
            String status
    ) {
        public static PaymentResponse from(PaymentInfo info) {
            if (info == null) return null;
            return new PaymentResponse(
                    info.id(),
                    info.orderId(),
                    info.amount(),
                    info.paidAt(),
                    info.status()
            );
        }
    }
}
