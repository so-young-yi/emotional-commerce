package com.loopers.application.like;

import com.loopers.domain.product.ProductMetaService;
import com.loopers.event.ProductLikedEvent;
import com.loopers.event.ProductUnlikedEvent;
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
class ProductLikeEventTest {

    @Autowired ProductLikeFacade productLikeFacade;

    @MockitoSpyBean
    ProductMetaService productMetaService;

    @MockitoSpyBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("좋아요/해제 시 Kafka로 이벤트가 발행된다")
    void like_then_kafka_events_published() {
        // Given
        Long userId = 99L;
        Long productId = 1001L;

        // When: 좋아요
        boolean liked = productLikeFacade.likeProduct(userId, productId);

        // Then
        Assertions.assertTrue(liked);
        verify(kafkaTemplate).send(anyString(), any());

        // When: 좋아요 해제
        boolean unliked = productLikeFacade.unlikeProduct(userId, productId);

        // Then
        Assertions.assertFalse(unliked);
        verify(kafkaTemplate, times(2)).send(anyString(), any()); // 총 2번 호출 (liked + unliked)
    }
}
