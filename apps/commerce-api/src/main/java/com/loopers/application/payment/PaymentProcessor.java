package com.loopers.application.payment;

import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PaymentMethod;

public interface PaymentProcessor<T extends Payment.Request<R>, R extends Payment.Response> {
    PaymentMethod method();
    R process(T request);
}
