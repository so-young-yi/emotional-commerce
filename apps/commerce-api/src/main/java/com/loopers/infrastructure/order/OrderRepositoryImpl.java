package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<OrderModel> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public OrderModel save(OrderModel order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<OrderModel> findByUserId(Long userId, Pageable pageable) {
        return orderJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<OrderModel> findByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public Page<OrderModel> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable);
    }
}
