package com.loopers.application.event;

import com.loopers.domain.order.OrderModel;

public record OrderCreatedEvent(Long orderId, Long userId, OrderModel order) {}
