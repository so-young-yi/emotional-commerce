package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void recordResult(Long orderId, PaymentResult result) {
        PaymentModel payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보 없음: " + orderId));
        payment.apply(result);
        paymentRepository.save(payment);
    }


    @Transactional
    public PaymentModel pay(Long orderId, Long amount) {
        PaymentModel payment = new PaymentModel(orderId, amount, ZonedDateTime.now(), PaymentStatus.PAID);
        return paymentRepository.save(payment);
    }

    @Transactional
    public void refund(Long paymentId) {
        PaymentModel payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 내역이 존재하지 않습니다."));
        payment.refund();
        paymentRepository.save(payment);
    }

    public PaymentModel getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream().findFirst()
                .orElse(null); // 결제 내역이 없을 수도 있으니 null 허용
    }

    public PaymentModel getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 내역이 존재하지 않습니다."));
    }

    public List<PaymentModel> getPaymentsByStatus(PaymentStatus status) {
        // 예시: 모든 결제 중 status가 일치하는 것만 반환
        // 실제로는 JPA 쿼리 등으로 구현
        return paymentRepository.findAll(Pageable.unpaged())
                .stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePaymentStatus(Long orderId, PaymentStatus status, String reason) {
        PaymentModel payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보 없음: " + orderId));
        payment.setStatus(status);
        payment.setReason(reason);
        paymentRepository.save(payment);
    }

}
