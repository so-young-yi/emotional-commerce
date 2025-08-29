package com.loopers.domain.payment.dto;

import com.loopers.domain.payment.PaymentStatus;
import lombok.*;

public class Payment {

    // 공통 인터페이스
    public interface Request<R extends Response> {
        Long getOrderId();
        Long getUserId();
        Long getAmount();
        PaymentMethod getPaymentMethod();
    }

    public interface Response {
        PaymentStatus getStatus();
        String getReason();
    }

    // 카드 결제
    @Getter
    @RequiredArgsConstructor
    public static class CardRequest implements Request<CardResponse> {
        private final Long orderId;
        private final Long userId;
        private final Long amount;
        private final String cardNo;
        private final CardType cardType;
        private final PgType pgType;

        @Override public PaymentMethod getPaymentMethod() { return PaymentMethod.CARD; }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CardResponse implements Response {
        private final PaymentStatus status;
        private final String transactionKey;
        private final String reason;
    }

    @Getter
    @RequiredArgsConstructor
    public static class PointRequest implements Request<PointResponse> {
        private final Long orderId;
        private final Long userId;
        private final Long amount;
        @Override public PaymentMethod getPaymentMethod() { return PaymentMethod.POINT; }
    }

    @Getter
    @RequiredArgsConstructor
    public static class PointResponse implements Response {
        private final PaymentStatus status;
        private final String reason;
    }

}
