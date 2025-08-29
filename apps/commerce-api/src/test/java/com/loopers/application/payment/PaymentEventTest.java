package com.loopers.application.payment;

import com.loopers.application.event.PaymentCompletedEvent;
import com.loopers.application.event.PaymentFailedEvent;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.external.pg.PgClientDto;
import com.loopers.interfaces.event.handler.OrderPaymentEventHandler;
import com.loopers.interfaces.event.handler.PointEventHandler;
import com.loopers.interfaces.event.handler.StockEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class PaymentEventTest {

    @Autowired
    PaymentFacade paymentFacade;
    @Autowired
    OrderService orderService;
    @Autowired
    PaymentService paymentService;

    @MockitoSpyBean
    OrderPaymentEventHandler orderPaymentEventHandler;

    @MockitoSpyBean
    StockEventHandler stockEventHandler;
    @MockitoSpyBean
    PointEventHandler pointEventHandler;

    @Nested
    @DisplayName("결제 콜백 → 이벤트 발행 → 후속 핸들러 처리")
    class CardPaymentCallback {

        Long orderId;

        @BeforeEach
        void setUp() {
            // Given: 주문 생성
            OrderModel order = orderService.createOrder(1L, TestOrders.sampleItems());
            orderId = order.getId();
            PaymentModel payment = paymentService.pay(order.getId(), order.getTotalAmount());
        }

        @Test
        @DisplayName("성공(PAID) 시 PaymentCompletedEvent 발행되고 주문 상태/재고/포인트 핸들러가 작동한다")
        void paid_flow() {
            // When: PG 콜백 수신 → PaymentFacade 처리
            PgClientDto.PgResponse pgResponse = new PgClientDto.PgResponse("tx-1", orderId.toString(), "SUCCESS", null);
            paymentFacade.handlePgCallback(pgResponse);

            // Then: 이벤트 핸들러 호출 검증 (비동기: timeout 사용)
            verify(orderPaymentEventHandler, timeout(1500)).onPaymentCompleted(any(PaymentCompletedEvent.class));
            verify(stockEventHandler,      timeout(1500)).onPaymentCompleted(any(PaymentCompletedEvent.class));
            verify(pointEventHandler,      timeout(1500)).onPaymentCompleted(any(PaymentCompletedEvent.class));

            // Then 2: 상태가 DB에 반영될 때까지 기다리며 검증 (eventual consistency)
            await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
                OrderModel updated = orderService.getOrderDetail(orderId);
                assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);

                PaymentModel payment = paymentService.getPaymentByOrderId(orderId);
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
                assertThat(payment.getTransactionKey()).isEqualTo("tx-1");
            });
        }

        @Test
        @DisplayName("결제 실패 시 PaymentFailedEvent가 발행되고 주문이 자동 취소된다")
        void failed_flow() {
            // Given
            OrderModel order = orderService.createOrder(1L, TestOrders.sampleItems());
            Long orderId = order.getId();
            paymentService.pay(orderId, order.getTotalAmount());

            // When
            PgClientDto.PgResponse pgResponse = new PgClientDto.PgResponse("tx-2", orderId.toString(), "FAILED", "DECLINED");
            paymentFacade.handlePgCallback(pgResponse);

            // Then
            verify(orderPaymentEventHandler, timeout(1500)).onPaymentFailed(any(PaymentFailedEvent.class));
            await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
                OrderModel updated = orderService.getOrderDetail(orderId);
                assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            });
        }
    }

    // 테스트 편의 Util (샘플 아이템 생성)
    static class TestOrders {
        static java.util.List<OrderItemModel> sampleItems() {
            return java.util.List.of(
                    new OrderItemModel(1001L, 1L, 10000L, "상품A"),
                    new OrderItemModel(1002L, 2L, 5000L, "상품B")
            );
        }
    }
}
