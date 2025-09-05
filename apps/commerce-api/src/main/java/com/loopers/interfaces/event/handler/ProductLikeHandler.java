package com.loopers.interfaces.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.ProductLikedEvent;
import com.loopers.application.event.ProductUnlikedEvent;
import com.loopers.interfaces.event.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductLikeHandler {

    private final ObjectMapper objectMapper;

    public void onProductLiked(ProductLikedEvent event) {
        log.info("좋아요 {} → {}", event.userId(), event.productId());
    }

    public void onProductUnliked(ProductUnlikedEvent event) {
        log.info("좋아요 해제 {} → {}", event.userId(), event.productId());
    }

    @KafkaListener(topics = "product-events", groupId = "collector")
    @Transactional
    public void consume(String message) throws Exception {
        KafkaMessage<Map<String, Object>> wrapper = objectMapper.readValue(
                message,
                new TypeReference<KafkaMessage<Map<String, Object>>>() {}
        );
        if ("ProductLikedEvent".equals(wrapper.type())) {
            ProductLikedEvent event = objectMapper.convertValue(wrapper.payload(), ProductLikedEvent.class);
            onProductLiked( event );
        } else if ("ProductUnlikedEvent".equals(wrapper.type())) {
            ProductUnlikedEvent event = objectMapper.convertValue(wrapper.payload(), ProductUnlikedEvent.class);
            onProductUnliked( event );
        }
    }


}
