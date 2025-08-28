package com.loopers.interfaces.event.handler;

import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductLikeHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductLiked(ProductLikedEvent event) {
        log.info("좋아요 {} → {}", event.userId(), event.productId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductUnliked(ProductUnlikedEvent event) {
        log.info("좋아요 해제 {} → {}", event.userId(), event.productId());
    }

}
