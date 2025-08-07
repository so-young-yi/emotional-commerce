package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.UserPointService;
import com.loopers.domain.product.ProductMetaService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private static final Logger log = LoggerFactory.getLogger(OrderFacade.class);
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final UserPointService pointService;
    private final ProductMetaService productMetaService;
    private final CouponService couponService;

    @Transactional
    public OrderInfo orderAndPay(Long userId, OrderV1Dto.OrderRequest request) {

        OrderModel order = createOrder(userId, request);

        long couponDiscount = 0L;
        if (request.couponId() != null) {
            couponDiscount = couponService.useCoupon(userId, request.couponId(), order.getTotalAmount());
        }

        long totalAmount = order.getTotalAmount() - couponDiscount;
        if (totalAmount < 0) totalAmount = 0;

        pointService.useUserPointWithLock(userId, totalAmount);

        PaymentModel payment = paymentService.pay(order.getId(), totalAmount);

        for (OrderItemModel orderItem : order.getOrderItems()) {
            productMetaService.decreaseStock(orderItem.getProductId(), orderItem.getQuantity());
        }

        return new OrderInfo(order, payment);
    }

    @Transactional
    public OrderModel createOrder(Long userId, OrderV1Dto.OrderRequest request) {
        List<OrderItemModel> orderItems = request.items().stream()
                .map(item -> {
                    ProductModel product = productService.getProductDetail(item.productId());
                    return new OrderItemModel(
                            product.getId(),
                            item.quantity(),
                            product.getSellPrice().getAmount(),
                            product.getName()
                    );
                })
                .collect(Collectors.toList());

        return orderService.createOrder(userId, orderItems);
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
