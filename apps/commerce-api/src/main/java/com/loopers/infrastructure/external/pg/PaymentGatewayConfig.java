package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.dto.PgType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentGatewayConfig {
    @Bean
    public Map<PgType, PaymentGateway> gatewayMap(
            PgSimulatorGateway pgSimulatorGateway,
            TossGateway tossGateway
    ) {
        Map<PgType, PaymentGateway> map = new HashMap<>();
        map.put(PgType.PG_SIMULATOR, pgSimulatorGateway);
        map.put(PgType.TOSS, tossGateway);
        return map;
    }
}
