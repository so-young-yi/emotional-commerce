package com.loopers.infrastructure.external.pg;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(
        name = "pgApiClient",
        url = "${client.pg-simulator.url.ver-1}",
        configuration = PgFeignConfig.class
)
public interface PgClient {

    @PostMapping("/api/v1/payments")
    ApiResponse<PgClientDto.PgResponse> request(@RequestBody PgClientDto.PgRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<PgClientDto.PgResponse> checkStatus(@PathVariable String transactionKey);

    @GetMapping("/api/v1/payments")
    ApiResponse<List<PgClientDto.PgResponse>> findByOrderId(@RequestParam String orderId);
}
