package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.PaymentModel;

public record OrderInfo(
        OrderModel order,
        PaymentModel payment
) { }
