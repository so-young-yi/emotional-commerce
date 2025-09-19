package com.loopers.application.order;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.interfaces.api.order.OrderV1Dto;
import com.loopers.support.Money;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class OrderCreationEventTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private OrderFacade orderFacade;

    @MockitoSpyBean
    KafkaTemplate<String, Object> kafkaTemplate;

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

    @Test
    @DisplayName("주문 생성 시 Kafka로 OrderCreatedEvent가 발행된다")
    void orderCreated_then_kafka_event_published() {
        // When
        orderFacade.createOrder(TEST_USER_ID, request);

        // Then
        verify(kafkaTemplate).send(anyString(), any());
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
