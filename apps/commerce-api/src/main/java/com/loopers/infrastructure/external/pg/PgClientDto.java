package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.dto.Payment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PgClientDto {

    public record PgRequest(
            String orderId,
            String cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {
        public static PgRequest from(Payment.CardRequest request, String callbackUrl) {
            return new PgRequest(
                    request.getOrderId().toString(),
                    request.getCardType().toString(),
                    request.getCardNo(),
                    request.getAmount().toString(),
                    callbackUrl
            );
        }
    }

    public record PgResponse(
            String transactionKey,
            String orderId,
            String status,
            String reason
    ) { }
}
