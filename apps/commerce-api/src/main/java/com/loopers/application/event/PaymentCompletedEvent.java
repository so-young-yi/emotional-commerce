package com.loopers.application.event;

import com.loopers.domain.payment.PaymentModel;

public record PaymentCompletedEvent(Long orderId, Long userId, PaymentModel payment) {}
