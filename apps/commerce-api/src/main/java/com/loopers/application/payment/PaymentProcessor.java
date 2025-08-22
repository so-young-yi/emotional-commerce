package com.loopers.application.payment;

import com.loopers.domain.payment.dto.Payment;

public interface PaymentProcessor<T extends Payment.Request, R extends Payment.Response> {
    R process(T request);
}
