package com.loopers.application.payment;

import com.loopers.domain.payment.dto.PaymentMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class PaymentProcessorConfig {
    @Bean
    public Map<PaymentMethod, PaymentProcessor<?, ?>> processorMap(
            PointPaymentProcessor pointProcessor,
            CardPaymentProcessor cardProcessor
    ) {
        Map<PaymentMethod, PaymentProcessor<?, ?>> map = new EnumMap<>(PaymentMethod.class);
        map.put(PaymentMethod.POINT, pointProcessor);
        map.put(PaymentMethod.CARD, cardProcessor);
        // 현금 등 추가
        return map;
    }
}
