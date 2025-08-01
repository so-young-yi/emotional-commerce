package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderModel, Long> {

    List<OrderModel> findByUserId(Long userId);

    Page<OrderModel> findByUserId(Long userId, Pageable pageable);

    Page<OrderModel> findAll(Pageable pageable);

    Optional<OrderModel> findById(Long id);
}
