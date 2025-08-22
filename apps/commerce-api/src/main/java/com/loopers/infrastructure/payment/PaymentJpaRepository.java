package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentModel, Long> {

    Optional<PaymentModel> findByOrderId(Long orderId);

    Page<PaymentModel> findByOrderId(Long orderId, Pageable pageable);

    Page<PaymentModel> findAll(Pageable pageable);
}
