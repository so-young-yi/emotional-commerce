package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentModel pay(Long orderId, Long amount) {
        PaymentModel payment = new PaymentModel(orderId, amount, ZonedDateTime.now(), PaymentStatus.PAID);
        return paymentRepository.save(payment);
    }

    public void refund(Long paymentId) {
        PaymentModel payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 내역이 존재하지 않습니다."));
        payment.refund(); // 상태를 REFUNDED로 변경
        paymentRepository.save(payment);
    }

    // 주문별 결제 내역 조회
    public PaymentModel getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream().findFirst()
                .orElse(null); // 결제 내역이 없을 수도 있으니 null 허용
    }

}
