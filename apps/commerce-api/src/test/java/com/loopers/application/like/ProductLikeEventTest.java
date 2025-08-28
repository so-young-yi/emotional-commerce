package com.loopers.application.like;

import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
import com.loopers.domain.product.ProductMetaService;
import com.loopers.interfaces.event.handler.AnalyticsHandler;
import com.loopers.interfaces.event.handler.ProductLikeHandler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class ProductLikeEventTest {

    @Autowired ProductLikeFacade productLikeFacade;

    @MockitoSpyBean
    ProductMetaService productMetaService;
    @MockitoSpyBean
    ProductLikeHandler productLikeHandler;
    @MockitoSpyBean
    private AnalyticsHandler analyticsHandler;

    @Test
    @DisplayName("좋아요/해제 시 집계/분석 핸들러가 비동기로 각각 호출된다")
    void like_then_aggregate_async() {
        // Given
        Long userId = 99L;
        Long productId = 1001L;

        // 좋아요
        boolean liked = productLikeFacade.likeProduct(userId, productId);
        Assertions.assertTrue(liked);
        verify(productLikeHandler, timeout(1500)).onProductLiked(any(ProductLikedEvent.class));
        verify(analyticsHandler, timeout(1500)).onProductLiked(any(ProductLikedEvent.class));

        // 좋아요 해제
        boolean unliked = productLikeFacade.unlikeProduct(userId, productId);
        Assertions.assertFalse(unliked);
        verify(productLikeHandler, timeout(1500)).onProductUnliked(any(ProductUnlikedEvent.class));
        verify(analyticsHandler, timeout(1500)).onProductUnliked(any(ProductUnlikedEvent.class));

    }
}
