package com.loopers.domain.order;

public enum OrderStatus {

    ORDERED,      // 주문 생성(결제 전)
    PAID,         // 결제 완료
    CANCELLED,    // 주문 취소(결제 전/후)
    FAILED,       // 결제 실패
    SHIPPED,      // 배송 시작
    DELIVERED,    // 배송 완료
    RETURNED,     // 반품 완료
    REFUNDED      // 환불 완료

}
