package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderModel createOrder(Long userId, List<OrderItemModel> orderItems) {
        OrderModel order = new OrderModel(userId, OrderStatus.ORDERED);
        for (OrderItemModel item : orderItems) {
            order.addOrderItem(item);
        }
        return orderRepository.save(order);
    }

    @Transactional
    public void markOrderAsPaid(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문이 존재하지 않습니다."));
        order.pay();
        orderRepository.save(order);
    }

    @Transactional
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
