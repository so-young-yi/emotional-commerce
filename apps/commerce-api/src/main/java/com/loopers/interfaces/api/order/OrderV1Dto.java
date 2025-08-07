package com.loopers.interfaces.api.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.PaymentModel;

import java.util.List;
import java.util.stream.Collectors;

public class OrderV1Dto {

    public record OrderRequest(
            List<OrderItem> items
    ){}

    public record OrderItem(
            Long productId,
            Long quantity
    ){}

    public record OrderResponse(
            Long id,
            Long userId,
            String createdAt,
            String status,
            List<OrderItemResponse> items,
            PaymentResponse payment
    ) {
        public static OrderResponse of(OrderModel order, PaymentModel payment) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(OrderItemResponse::of)
                    .collect(Collectors.toList());

            return new OrderResponse(
                    order.getId(),
                    order.getUserId(),
                    order.getCreatedAt().toString(),
                    order.getStatus().name(),
                    itemResponses,
                    payment != null ? PaymentResponse.of(payment) : null
            );
        }
    }

    public record OrderItemResponse(
            Long orderItemId,
            Long productId,
            String productNameSnapshot,
            Long quantity,
            Long priceSnapshot
    ) {
        public static OrderItemResponse of(OrderItemModel item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getProductNameSnapshot(),
                    item.getQuantity(),
                    item.getPriceSnapshot()
            );
        }
    }

    public record PaymentResponse(
            Long paymentId,
            Long orderId,
            Long amount,
            String paidAt,
            String status
    ) {
        public static PaymentResponse of(PaymentModel payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getPaidAt() != null ? payment.getPaidAt().toString() : null,
                    payment.getStatus().name()
            );
        }
    }
}
