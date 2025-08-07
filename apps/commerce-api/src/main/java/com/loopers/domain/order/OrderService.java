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

    public void decreaseStocks(OrderModel order) {
        order.getOrderItems().forEach(item -> {
            productService.decreaseStock(item.getProductId(), item.getQuantity());
        });
    }

    public void cancelOrder(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문이 존재하지 않습니다."));
        order.cancel();
        orderRepository.save(order);
    }

    public List<OrderModel> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public OrderModel getOrderDetail(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문이 존재하지 않습니다."));
    }
}
