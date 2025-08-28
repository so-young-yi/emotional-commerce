package com.loopers.application.order;

import com.loopers.application.event.OrderCreatedEvent;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.interfaces.event.handler.AnalyticsHandler;
import com.loopers.interfaces.event.handler.OrderNotificationHandler;
import com.loopers.support.Money;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@RecordApplicationEvents
class OrderCreationEventTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private OrderFacade orderFacade;

    @MockitoSpyBean
    private AnalyticsHandler analyticsHandler;
    @MockitoSpyBean private OrderNotificationHandler notificationHandler;

    @Autowired private ApplicationEvents events;

    private static final Long TEST_USER_ID = 10L;
    private OrderV1Dto.OrderRequest request;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 저장
        ProductModel product = new ProductModel(
                null,
                1L,
                "에어맥스",
                "에어맥스 설명",
                new Money(100000L),
                ProductStatus.ON_SALE,
                java.time.ZonedDateTime.now()
        );
        productRepository.save(product);

        // 공통 주문 요청 세팅
        request = TestOrderRequests.simple();
    }

    @Nested
    @DisplayName("AnalyticsHandler 이벤트 처리")
    class AnalyticsHandlerTests {

        @Test
        @DisplayName("주문 생성 시 AnalyticsHandler가 호출된다")
        void analyticsHandlerReceivesEvent() {
            orderFacade.createOrder(TEST_USER_ID, request);

            verify(analyticsHandler, timeout(1500)).onOrderCreated(any(OrderCreatedEvent.class));
            assertThat(events.stream(OrderCreatedEvent.class).count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("OrderNotificationHandler 이벤트 처리")
    class NotificationHandlerTests {

        @Test
        @DisplayName("주문 생성 시 NotificationHandler가 호출된다")
        void notificationHandlerReceivesEvent() {
            orderFacade.createOrder(TEST_USER_ID, request);

            verify(notificationHandler, timeout(1500)).onOrderCreated(any(OrderCreatedEvent.class));
            assertThat(events.stream(OrderCreatedEvent.class).count()).isEqualTo(1);
        }
    }

    static class TestOrderRequests {
        static OrderV1Dto.OrderRequest simple() {
            return new OrderV1Dto.OrderRequest(
                    java.util.List.of(new OrderV1Dto.OrderItem(1L, 1L, null)),
                    null
            );
        }
    }
}
