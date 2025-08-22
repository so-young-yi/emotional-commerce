package com.loopers.application.payment;

import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PgType;
import com.loopers.infrastructure.external.pg.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component("CARD")
public class CardPaymentProcessor implements PaymentProcessor<Payment.CardRequest, Payment.CardResponse> {

    private final Map<PgType, PaymentGateway> gatewayMap; // PG 종류별로 주입

    @Override
    public Payment.CardResponse process(Payment.CardRequest request) {

        PgType pgType = request.getPgType();
        PaymentGateway gateway = gatewayMap.get(pgType);
        if (gateway == null) throw new IllegalArgumentException("지원하지 않는 PG: " + pgType);

        String callbackUrl = "http://localhost:8080/api/v1/payment/callback";
        return gateway.requestPayment(request, callbackUrl);
    }

}
