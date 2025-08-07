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
    public OrderInfo orderAndPay(Long userId, OrderV1Dto.OrderRequest request) {
        OrderModel order = orderService.createOrder(userId, request);

        long totalAmount = order.getTotalAmount();

        pointService.useUserPoint(userId, totalAmount);

        PaymentModel payment = paymentService.pay(order.getId(), totalAmount);

        orderService.decreaseStocks(order);

        return new OrderInfo(order, payment);
    }

    public List<OrderInfo> getOrders(Long userId) {
        List<OrderModel> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
                .map(order -> {
                    PaymentModel payment = paymentService.getPaymentByOrderId(order.getId());
                    return new OrderInfo(order, payment);
                })
                .collect(Collectors.toList());
    }

    public OrderInfo getOrder(Long userId, Long orderId) {
        OrderModel order = orderService.getOrderDetail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 주문에 접근할 권한이 없습니다.");
        }
        PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
        return new OrderInfo(order, payment);
    }

}
