package com.loopers.application.payment;

import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.PaymentFailedEvent;
import com.loopers.application.event.kafka.KafkaEventPublisher;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.dto.Payment;
import com.loopers.domain.payment.dto.PaymentMethod;
import com.loopers.infrastructure.external.pg.PgClientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final Map<PaymentMethod, PaymentProcessor<?, ?>> processorMap;
    private final KafkaEventPublisher eventPublisher;

    @SuppressWarnings("unchecked")
    public <R extends Payment.Response> R pay(Payment.Request<R> request) {
        PaymentProcessor<Payment.Request<R>, R> processor =
                (PaymentProcessor<Payment.Request<R>, R>) processorMap.get(request.getPaymentMethod());
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }
        return processor.process(request);
    }

    public PaymentInfo getPayment(Long paymentId) {
        PaymentModel payment = paymentService.getPayment(paymentId);
        return PaymentInfo.from(payment);
    }

    public PaymentInfo getPaymentByOrderId(Long orderId) {
        PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) return null;
        return PaymentInfo.from(payment);
    }

    public void refund(Long paymentId) {
        paymentService.refund(paymentId);
    }

    @Transactional
    public void handlePgCallback(PgClientDto.PgResponse pgResponse) {
        PaymentResult result = PaymentResult.fromPgResponse(pgResponse);
        Long orderId = Long.valueOf(pgResponse.orderId());
        paymentService.recordResult(orderId, result);
        OrderModel orderModel = orderService.getOrderDetail(orderId);

        if (result.status() == PaymentStatus.PAID) {
            orderService.markOrderAsPaid(orderId);
            PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
            eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, orderModel.getUserId(), payment));
        } else if (result.status() == PaymentStatus.FAILED) {
            orderService.cancelOrder(orderId);
            eventPublisher.publishEvent(new PaymentFailedEvent(orderId, orderModel.getUserId(), result.reason()));
        }
    }
}
