package com.loopers.application.payment;

import com.loopers.domain.payment.dto.PaymentMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PaymentProcessorConfig {
    @Bean
    public Map<PaymentMethod, PaymentProcessor<?, ?>> processorMap(
            List<PaymentProcessor<?, ?>> processors
    ) {
        Map<PaymentMethod, PaymentProcessor<?, ?>> map = new EnumMap<>(PaymentMethod.class);
        for (PaymentProcessor<?, ?> p : processors) {
            map.put(p.method(), p);
        }
        return map;
    }
}
