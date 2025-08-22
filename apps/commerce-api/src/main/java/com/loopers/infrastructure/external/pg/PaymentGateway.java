package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.Payment;

public interface PaymentGateway {

    Payment.CardResponse requestPayment(Payment.CardRequest request, String callbackUrl);
    PaymentStatus checkPaymentStatus(String transactionKey);
    String getTransactionKey(Long orderId);
}
