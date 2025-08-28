package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PaymentMethod;
import com.loopers.infrastructure.external.pg.PaymentGateway;
import com.loopers.infrastructure.external.pg.PgGatewayResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CardPaymentProcessor implements PaymentProcessor<Payment.CardRequest, Payment.CardResponse> {

    private final PgGatewayResolver pgResolver;
    private final PaymentService paymentService;

    @Value("${client.pg-simulator.callback-url.v1}")
    private String callbackUrl;

    @Override
    public PaymentMethod method() { return PaymentMethod.CARD; }

    @Override
    public Payment.CardResponse process(Payment.CardRequest request) {
        PaymentGateway gateway = pgResolver.resolve(request.getPgType());
        Payment.CardResponse response = gateway.requestPayment(request, callbackUrl);
        PaymentResult result = PaymentResult.fromCardResponse(response);
        paymentService.recordResult(request.getOrderId(), result);
        return response;
    }
}
