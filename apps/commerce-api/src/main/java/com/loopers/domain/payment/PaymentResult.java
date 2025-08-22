package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.Payment;
import com.loopers.infrastructure.external.pg.PgClientDto;

public record PaymentResult(PaymentStatus status, String reason, String transactionKey) {

    public static PaymentResult fromPgResponse(PgClientDto.PgResponse response) {
        PaymentStatus status = switch (response.status()) {
            case "SUCCESS" -> PaymentStatus.PAID;
            case "FAILED" -> PaymentStatus.FAILED;
            case "PENDING" -> PaymentStatus.PENDING;
            default -> PaymentStatus.INIT;
        };
        return new PaymentResult(status, response.reason(), response.transactionKey());
    }

    public Payment.CardResponse toCardResponse() {
        return new Payment.CardResponse(status, transactionKey, reason);
    }

    public Payment.PointResponse toPointResponse() {
        return new Payment.PointResponse(status, reason);
    }
}
