package com.loopers.interfaces.api.payment;

import com.loopers.infrastructure.external.pg.PgClientDto;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment V1 Api", description = "결제 api")
public interface PaymentV1ApiSpec {

    @Operation(summary = "결제 단건 조회", description = "결제 ID로 결제 내역을 조회합니다.")
    @GetMapping("/{paymentId}")
    ApiResponse<PaymentV1Dto.PaymentResponse> getPayment(@PathVariable Long paymentId);

    @Operation(summary = "주문별 결제 내역 조회", description = "주문 ID로 결제 내역을 조회합니다.")
    @GetMapping("/order/{orderId}")
    ApiResponse<PaymentV1Dto.PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId);

    @Operation(summary = "결제 환불", description = "결제 ID로 환불을 처리합니다.")
    @PostMapping("/{paymentId}/refund")
    ApiResponse<Void> refund(@PathVariable Long paymentId);

    @Operation(summary = "PG 결제 결과 콜백", description = "PG에서 결제 결과를 콜백으로 전달합니다.")
    @PostMapping("/callback")
    ApiResponse<Void> handlePgCallback(@RequestBody PgClientDto.PgResponse pgResponse);

}
