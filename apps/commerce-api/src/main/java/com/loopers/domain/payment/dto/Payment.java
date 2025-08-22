package com.loopers.domain.payment.dto;

import com.loopers.domain.payment.PaymentStatus;
import lombok.*;

public class Payment {

    // 공통 인터페이스
    public interface Request {
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
    public static class CardRequest implements Request {
        private final Long orderId;
        private final Long userId;
        private final Long amount;
        private final String cardNo;
        private final CardType cardType;
        private final PgType pgType;

        @Override public PaymentMethod getPaymentMethod() { return PaymentMethod.CARD; }
        public PgType getPgType() { return pgType; }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CardResponse implements Response {
        private final PaymentStatus status;
        private final String transactionKey;
        private final String reason;
    }

    // 포인트 결제
    @Getter
    @RequiredArgsConstructor
    public static class PointRequest implements Request {
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

    // 현금 등 다른 결제수단도 동일하게 추가 가능
}
