package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.UserPointService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserPointService pointService;

    @Transactional
    public OrderV1Dto.OrderResponse orderAndPay(Long userId, OrderV1Dto.OrderRequest request) {
        // 1. 주문 생성 (아직 재고 차감 X)
        OrderModel order = orderService.createOrder(userId, request);

        // 2. 결제 금액 계산
        long totalAmount = order.getOrderItems().stream()
                .mapToLong(item -> item.getPriceSnapshot() * item.getQuantity())
                .sum();

        // 3. 포인트 차감
        pointService.useUserPoint(userId, totalAmount);

        // 4. 결제 생성 및 저장
        PaymentModel payment = paymentService.pay(order.getId(), totalAmount);

        // 5. 결제 성공 후 상품 재고 차감
        try {
            orderService.decreaseStocks(order); // 주문에 포함된 모든 상품 재고 차감
        }
        catch (Exception e) {
            // 재고 부족 등 예외 발생 시 결제/포인트 환불, 주문 취소 등 처리
            paymentService.refund(payment.getId());
            pointService.refundUserPoint(userId, totalAmount);
            orderService.cancelOrder(order.getId());
            throw new CoreException(ErrorType.CONFLICT, "결제 후 재고 부족: 결제/포인트 환불 및 주문 취소");
        }

        // 6. 응답 DTO 조립
        return OrderV1Dto.OrderResponse.of(order, payment);
    }

    // 주문 목록 조회 (유저별)
    public List<OrderV1Dto.OrderResponse> getOrders(Long userId) {
        List<OrderModel> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
                .map(order -> {
                    PaymentModel payment = paymentService.getPaymentByOrderId(order.getId());
                    return OrderV1Dto.OrderResponse.of(order, payment);
                })
                .collect(Collectors.toList());
    }

    // 주문 단건 상세 조회
    public OrderV1Dto.OrderResponse getOrder(Long userId, Long orderId) {
        OrderModel order = orderService.getOrderDetail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 주문에 접근할 권한이 없습니다.");
        }
        PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
        return OrderV1Dto.OrderResponse.of(order, payment);
    }

}
