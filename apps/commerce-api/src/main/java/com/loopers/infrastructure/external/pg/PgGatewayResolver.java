package com.loopers.infrastructure.external.pg;

import com.loopers.domain.payment.dto.PgType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PgGatewayResolver {
    private final Map<PgType, PaymentGateway> gatewayMap;

    public PaymentGateway resolve(PgType type) {
        PaymentGateway gw = gatewayMap.get(type);
        if (gw == null) throw new IllegalArgumentException("지원하지 않는 PG: " + type);
        return gw;
    }
}
