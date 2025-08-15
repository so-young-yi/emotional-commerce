package com.loopers.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<PaymentModel> findById(Long id);

    PaymentModel save(PaymentModel payment);

    Page<PaymentModel> findByOrderId(Long orderId, Pageable pageable);

    List<PaymentModel> findByOrderId(Long orderId);

    Page<PaymentModel> findAll(Pageable pageable);
}
