package com.loopers.domain.order;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderModel createOrder(Long userId, OrderV1Dto.OrderRequest request) {
        OrderModel order = new OrderModel(userId, OrderStatus.ORDERED);

        for (OrderV1Dto.OrderItem item : request.items()) {
            ProductModel product = productService.getProductDetail(item.productId());
            OrderItemModel orderItem = new OrderItemModel(
                    product.getId(),
                    item.quantity(),
                    product.getSellPrice().getAmount(),
                    product.getName()
            );
            order.addOrderItem(orderItem);
        }

        return orderRepository.save(order);
    }

    /**
     * 주문에 포함된 모든 상품의 재고를 차감한다.
     * 재고 부족/주문불가 상품이 있으면 예외를 던진다.
     */
    public void decreaseStocks(OrderModel order) {
        order.getOrderItems().forEach(item -> {
            productService.decreaseStock(item.getProductId(), item.getQuantity());
        });
    }

    public void cancelOrder(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문이 존재하지 않습니다."));
        order.cancel(); // 상태를 CANCELLED로 변경
        orderRepository.save(order);
    }

    // 유저별 주문 목록 조회
    public List<OrderModel> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // 주문 단건 상세 조회
    public OrderModel getOrderDetail(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문이 존재하지 않습니다."));
    }


}
