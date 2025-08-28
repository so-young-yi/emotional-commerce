package com.loopers.application.event;

public record PaymentFailedEvent(Long orderId, Long userId, String reason) {}
