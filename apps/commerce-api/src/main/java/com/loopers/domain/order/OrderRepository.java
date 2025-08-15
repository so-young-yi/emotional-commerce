package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<OrderModel> findById(Long id);

    OrderModel save(OrderModel order);

    Page<OrderModel> findByUserId(Long userId, Pageable pageable);

    List<OrderModel> findByUserId(Long userId);

    Page<OrderModel> findAll(Pageable pageable);
}
